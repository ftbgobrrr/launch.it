package launchit.auth.events;

import launchit.auth.model.Profile;

public interface IAuthListener {

    public void loginEvent(Throwable error, Profile profile);
    public void refreshEvent(Throwable error, Profile profile);
    public void logoutEvent(Throwable error, Profile profile);

}
