package auroracafe.model;

import java.io.Serializable;

public class CafeTable implements Serializable {
    private final int id;
    private String name;
    private int seats;
    private TableStatus status;
    private Integer currentOrderId;

    public CafeTable(int id, String name, int seats, TableStatus status) {
        this.id = id;
        this.name = name;
        this.seats = seats;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    public Integer getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Integer currentOrderId) { this.currentOrderId = currentOrderId; }

    @Override
    public String toString() {
        String statusText = switch (status) {
            case AVAILABLE -> "Bàn trống";
            case OCCUPIED -> "Đang phục vụ";
            case RESERVED -> "Đã đặt trước";
        };
        return name + " • " + statusText + " • " + seats + " ghế";
    }
}
