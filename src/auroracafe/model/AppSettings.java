package auroracafe.model;

import java.io.Serializable;

public class AppSettings implements Serializable {
    private String businessName = "Anh Kiệt CAFÉ";
    private String slogan = "Cà phê đẹp • Phục vụ nhanh • Quản lý thông minh";
    private double taxRate = 0.08;
    private double serviceChargeRate = 0.05;
    private String invoiceFooter = "Cảm ơn bạn đã ghé Anh Kiệt CAFÉ. Hẹn gặp lại bạn ở ly cà phê tiếp theo.";
    private String accentColorHex = "#8B5CF6";

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getSlogan() { return slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public double getServiceChargeRate() { return serviceChargeRate; }
    public void setServiceChargeRate(double serviceChargeRate) { this.serviceChargeRate = serviceChargeRate; }
    public String getInvoiceFooter() { return invoiceFooter; }
    public void setInvoiceFooter(String invoiceFooter) { this.invoiceFooter = invoiceFooter; }
    public String getAccentColorHex() { return accentColorHex; }
    public void setAccentColorHex(String accentColorHex) { this.accentColorHex = accentColorHex; }
}
