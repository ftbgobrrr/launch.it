package launchit.auth.events;

import launchit.auth.model.Profile;

public interface IAuthListener {

    public void loginEvent(Throwable error, Profile profile);

}
