package com.sms.billing.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sms.billing.entity.Plan;
import com.sms.billing.event.PlanCreatedEvent;
import com.sms.billing.repository.PlanRepository;
import com.sms.billing.service.PlanService;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class PlanEventListener {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private final ObjectMapper objectMapper;
    private final PlanService planService;
    private final PlanRepository planRepository;
    //private final RazorpayClient razorpayClient;
    public static final String PLAN_CREATED_TOPIC = "plan.created";

    public PlanEventListener(ObjectMapper objectMapper,
                             PlanService planService,
                             PlanRepository planRepository) throws RazorpayException {
        this.objectMapper = objectMapper;
        this.planService = planService;
        this.planRepository = planRepository;
        // Initialize Razorpay client with your keys
        //this.razorpayClient = new RazorpayClient(keyId, keySecret);
    }

    //@KafkaListener(topics = "plan.created", groupId = "${spring.kafka.consumer.group-id:plan-created-group}")
    //@Transactional
    @KafkaListener(topics = PlanEventListener.PLAN_CREATED_TOPIC, groupId = "plan-service")
    @Transactional
    public void onPlanCreated(String rawJson) {
        PlanCreatedEvent event;
        try {
            event = objectMapper.readValue(rawJson, PlanCreatedEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Could not parse PlanCreatedEvent, dropping message: {}", rawJson, e);
            return;
        }

        if (planRepository.existsByPlanIdAndOrganizationId(event.planId(), event.organizationId())) {
            log.info("PlanCreatedEvent {} already processed for this organization- skipping duplicate delivery", event.planId());
            return;
        }

        // Save locally
        Plan plan = new Plan();
        plan.setPlanId(event.planId());
        plan.setProductId(event.productId());
        plan.setOrganizationId(event.organizationId());
        plan.setBillingCycle(event.billingCycle());
        plan.setName(event.name());
        plan.setCurrency(event.currency());
        plan.setAmount(event.unitAmount());
        plan.setCreatedAt(LocalDateTime.now());

        try {
            // Call Razorpay Plan API
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);
            JSONObject planRequest = new JSONObject();
            planRequest.put("period",event.billingCycle().toLowerCase());
            planRequest.put("interval",1);
            JSONObject item = new JSONObject();
            item.put("name",event.name());
            item.put("amount",event.unitAmount().multiply(new java.math.BigDecimal(100)));
            item.put("currency",event.currency());
            item.put("description","Description for the test plan");
            planRequest.put("item",item);
            JSONObject notes = new JSONObject();
            notes.put("notes_key_1","Monthly plan");
            notes.put("notes_key_2","Each month pay via CC");
            planRequest.put("notes",notes);

            com.razorpay.Plan razorpayPlan = razorpay.plans.create(planRequest);

            plan.setRazorpayPlanId(razorpayPlan.get("id"));
            log.info("Created Razorpay plan {} for local plan {}", plan.getRazorpayPlanId(), plan.getPlanId());


        } catch (RazorpayException ex) {
            log.error("Failed to create Razorpay plan for {}", event.planId(), ex);
            // Decide: retry, DLQ, or mark plan as unsynced
        }

        planRepository.save(plan);
        log.info("Persisted plan {} with Razorpay sync", plan.getPlanId());
    }
}
