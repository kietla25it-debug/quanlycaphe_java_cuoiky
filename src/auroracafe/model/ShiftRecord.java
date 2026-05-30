package auroracafe.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ShiftRecord implements Serializable {
    private final int id;
    private final int userId;
    private final String userName;
    private final LocalDateTime clockIn;
    private LocalDateTime clockOut;

    public ShiftRecord(int id, int userId, String userName, LocalDateTime clockIn) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.clockIn = clockIn;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public LocalDateTime getClockIn() { return clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
    public boolean isOpen() { return clockOut == null; }
    public LocalDate getDate() { return clockIn.toLocalDate(); }

    public long getWorkedMinutes() {
        LocalDateTime end = clockOut == null ? LocalDateTime.now() : clockOut;
        return Math.max(0, Duration.between(clockIn, end).toMinutes());
    }

    public double getWorkedHours() {
        return getWorkedMinutes() / 60.0;
    }
}
