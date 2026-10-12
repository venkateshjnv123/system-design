package org.example.parkingLot.paymentStrategy;

public class Razorpay implements PaymentStrategy{
    @Override
    public boolean processPayment(Double amount) {
        System.out.println("Payment accepted through razorpay: " + amount);
        return true;
    }
}
