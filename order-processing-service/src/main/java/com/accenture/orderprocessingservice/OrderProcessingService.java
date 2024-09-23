package com.accenture.orderprocessingservice;



import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderProcessingService {

    private final Random random = new Random();

    private final Map<Long, Order> orderStorage = new ConcurrentHashMap<>();

    private final KafkaOrderProducer kafkaOrderProducer;


    // Process the order by validating it and recalculating the price.
     public void processOrder(Order order) {
         // Set the status to PROCESSING
         order.setStatus(OrderStatus.PROCESSING);

         try {
             // Simulate some validation logic (e.g., check stock, customer credit, etc.)
             validateOrder(order);

             // Simulate recalculating the order price
             recalculateOrderPrice(order);

             // If everything is successful, mark the order as COMPLETED
             order.setStatus(OrderStatus.COMPLETED);

             orderStorage.putIfAbsent(order.getId(), order);

         } catch (Exception e) {
             // In case of any failure, mark the order as FAILED
             order.setStatus(OrderStatus.FAILED);
         }

         // Simulating delay
         try {
             Thread.sleep(random.nextInt(400) + 100);  // Random delay between 100-500ms
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }


         // Send the processed order back to Kafka
         try {
             kafkaOrderProducer.sendProcessedOrder(order);
         } catch (Exception e) {
             throw new RuntimeException("Failed to send processed order to Kafka", e);
         }
     }

    // Validacion del orden
    private void validateOrder(Order order) {

        // Checking for items in the order
        if (order.getOrderItems().isEmpty()) {
            throw new ValidateException("Order must contain at least one item.");
        }
    }

    // Applying a random discount
    private void recalculateOrderPrice(Order order) {

        BigDecimal totalAmount = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulate applying a random discount
        BigDecimal discount = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0, 0.1));
        totalAmount = totalAmount.subtract(totalAmount.multiply(discount));

        order.setOrderAmount(totalAmount);
    }
}