package com.sdlproject.salesandrepair.models;

public class ModelRepairUser {
    String repairId, productId, repairDescription, repairingCharges, inWarranty, customerEmail, repairStatus, timestamp, repairTo;

    public ModelRepairUser() {
    }

    public ModelRepairUser(String repairId, String productId, String repairDescription, String repairingCharges, String inWarranty, String customerEmail, String repairStatus, String timestamp, String repairTo) {
        this.repairId = repairId;
        this.productId = productId;
        this.repairDescription = repairDescription;
        this.repairingCharges = repairingCharges;
        this.inWarranty = inWarranty;
        this.customerEmail = customerEmail;
        this.repairStatus = repairStatus;
        this.timestamp = timestamp;
        this.repairTo = repairTo;
    }

    public String getRepairId() {
        return repairId;
    }

    public void setRepairId(String repairId) {
        this.repairId = repairId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRepairDescription() {
        return repairDescription;
    }

    public void setRepairDescription(String repairDescription) {
        this.repairDescription = repairDescription;
    }

    public String getRepairingCharges() {
        return repairingCharges;
    }

    public void setRepairingCharges(String repairingCharges) {
        this.repairingCharges = repairingCharges;
    }

    public String getInWarranty() {
        return inWarranty;
    }

    public void setInWarranty(String inWarranty) {
        this.inWarranty = inWarranty;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(String repairStatus) {
        this.repairStatus = repairStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRepairTo() {
        return repairTo;
    }

    public void setRepairTo(String repairTo) {
        this.repairTo = repairTo;
    }
}
