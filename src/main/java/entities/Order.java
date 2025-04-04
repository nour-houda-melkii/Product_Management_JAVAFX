package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String reference;
    private String customerEmail;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
    }

    public Order(String reference, String customerEmail, double totalAmount) {
        this();
        this.reference = reference;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.status = "Confirmed";
    }

    public Order(int id, String reference, String customerEmail, LocalDateTime orderDate,
                 double totalAmount, String status) {
        this();
        this.id = id;
        this.reference = reference;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Add item to the order
    public void addItem(OrderItem item) {
        items.add(item);
        // Update total if needed
        updateTotal();
    }

    // Calculate total based on items
    public void updateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        updateTotal();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", items=" + items.size() +
                '}';
    }
}