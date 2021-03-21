package launchit.events.factory;

public class Event {

    public boolean canceled = false;

    public boolean isCancelable() {
        return false;
    }

    public boolean isCanceled() {
        return isCancelable() && canceled;
    }

    public void setCanceled(boolean c) {
        this.canceled = c;
    }
}
