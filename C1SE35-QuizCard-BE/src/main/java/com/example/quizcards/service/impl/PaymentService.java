package com.example.quizcards.service.impl;

import com.example.quizcards.dto.request.PaymentRequest;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.CategorySubscription;
import com.example.quizcards.entities.UserSubscription;
import com.example.quizcards.repository.ICategorySubscriptionRepository;
import com.example.quizcards.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    @Value("${stripe.apiKey}")
    private String stripeApiKey;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;
    @Autowired
    private ICategorySubscriptionRepository categorySubscriptionRepository;

    @Autowired
    private AppUserServiceImpl appUserService;

    public String processPayment(PaymentRequest paymentRequest) throws StripeException {
        try {

            System.out.println(paymentRequest);

            Optional<CategorySubscription> subscription = categorySubscriptionRepository.findByName(paymentRequest.getSubcription());
            System.out.println(subscription);
            if (subscription.isEmpty()) {
                return "Subscription plan not found!";
            }


            UserSubscription userSubscription = userSubscriptionRepository.findByAppUser_UserId(paymentRequest.getUserId()).orElse(null);
            if (userSubscription != null && userSubscription.getStatusPaid() == UserSubscription.StatusPaid.PAID &&
                    userSubscription.getExpiredDate().after(new Timestamp(System.currentTimeMillis()))) {
                return "Your premium account is still active.";
            }


            com.stripe.Stripe.apiKey = stripeApiKey;


            BigDecimal amount = paymentRequest.getAmount();


            Map<String, Object> paymentIntentParams = new HashMap<>();
            paymentIntentParams.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue()); // Chuyển đổi sang cent
            paymentIntentParams.put("currency", "usd");
            paymentIntentParams.put("payment_method", paymentRequest.getPaymentMethodId());
            paymentIntentParams.put("confirmation_method", "manual");
            paymentIntentParams.put("confirm", true);


            PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);


            if (!paymentIntent.getStatus().equals("succeeded")) {
                return "Payment failed.";
            }


            if (userSubscription == null) {

                userSubscription = new UserSubscription();
                userSubscription.setAppUser(new AppUser(paymentRequest.getUserId()));
                userSubscription.setStatusPaid(UserSubscription.StatusPaid.PAID);
                userSubscription.setTokenPayment(paymentIntent.getId());
                userSubscription.setType(UserSubscription.TokenType.CARD);
                userSubscription.setCategorySubscription(subscription.orElse(null));
            } else {

                userSubscription.setStatusPaid(UserSubscription.StatusPaid.PAID);
                userSubscription.setTokenPayment(paymentIntent.getId());
            }
            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());

            Timestamp newExpiryDate = new Timestamp(currentTimestamp.getTime());

            switch (paymentRequest.getPlanType().toLowerCase()) {
                case "monthly":
                    newExpiryDate.setMonth(newExpiryDate.getMonth() + 1);
                    break;
                case "annually":
                    newExpiryDate.setYear(newExpiryDate.getYear() + 1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid plan type: " + paymentRequest.getPlanType());
            }
            userSubscription.setExpiredDate(newExpiryDate);

            userSubscription.setCategorySubscription(subscription.get());

            userSubscriptionRepository.save(userSubscription);

            appUserService.updateUserRole(paymentRequest.getUserId(), subscription.get().getId());

            return "Payment successful! Your account has been upgraded to Premium.";
        } catch (StripeException e) {

            e.printStackTrace();
            return "Payment failed due to Stripe error: " + e.getMessage();
        } catch (Exception e) {

            e.printStackTrace();
            return "An error occurred: " + e.getMessage();
        }

    }
};