package auroracafe.model;

import java.time.LocalDate;
import java.io.Serializable;

public class DailySalesStats implements Serializable {
    private static final long serialVersionUID = 1L;
    private final LocalDate date;
    private final int paidOrders;
    private final int cupsSold;
    private final double revenue;

    public DailySalesStats(LocalDate date, int paidOrders, int cupsSold, double revenue) {
        this.date = date;
        this.paidOrders = paidOrders;
        this.cupsSold = cupsSold;
        this.revenue = revenue;
    }

    public LocalDate getDate() { return date; }
    public int getPaidOrders() { return paidOrders; }
    public int getCupsSold() { return cupsSold; }
    public double getRevenue() { return revenue; }
}
