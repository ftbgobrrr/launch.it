package launchit.events.factory;

public class Cancelable extends Event {

    public boolean isCancelable() {
        return true;
    }

}
