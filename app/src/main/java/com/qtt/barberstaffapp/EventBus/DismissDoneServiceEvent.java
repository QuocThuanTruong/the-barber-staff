package com.qtt.barberstaffapp.EventBus;

public class DismissDoneServiceEvent {
    private Boolean isDismiss;

    public DismissDoneServiceEvent(Boolean isDismiss) {
        this.isDismiss = isDismiss;
    }

    public Boolean getDismiss() {
        return isDismiss;
    }

    public void setDismiss(Boolean dismiss) {
        isDismiss = dismiss;
    }
}
