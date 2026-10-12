package org.example.parkingLot;

import org.example.parkingLot.paymentStrategy.PaymentStrategy;
import org.example.parkingLot.paymentStrategy.Razorpay;

public class PaymentService {
    Razorpay razorpay = new Razorpay();

    boolean pay(double amount, String strategy){
        switch (strategy){
            case "razorpay":
                razorpay.processPayment(amount);
                return true;
            case "payu":
                System.out.println("Payu is not working currently");
                return false;
            default:
                System.out.println("Payment method is not working");
                return false;
        }
    }
}
