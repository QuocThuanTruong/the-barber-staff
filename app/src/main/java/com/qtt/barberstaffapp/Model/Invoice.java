package com.qtt.barberstaffapp.Model;

import java.util.List;

public class Invoice {
    private String salonId, salonName, salonAddress;
    private String barberId, barberName;
    private String customerName, customerPhone;
    private String imgUrl;
    private List<CartItem> shoppingItemList;
    private List<BarberServices> barberServicesList;
    private double finalPrice;

    public Invoice() {
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getSalonAddress() {
        return salonAddress;
    }

    public void setSalonAddress(String salonAddress) {
        this.salonAddress = salonAddress;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


    public List<CartItem> getShoppingItemList() {
        return shoppingItemList;
    }

    public void setShoppingItemList(List<CartItem> shoppingItemList) {
        this.shoppingItemList = shoppingItemList;
    }

    public List<BarberServices> getBarberServicesList() {
        return barberServicesList;
    }

    public void setBarberServicesList(List<BarberServices> barberServicesList) {
        this.barberServicesList = barberServicesList;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}
