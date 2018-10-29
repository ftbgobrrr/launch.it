package launchit.events;

import launchit.auth.model.Profile;
import launchit.events.factory.Event;

public class AuthEvent extends Event {

    private final Throwable error;

    public AuthEvent(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public static class Login extends AuthEvent {

        private final Profile profile;

        public Login(Throwable error, Profile profile) {
            super (error);
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }
    }

    public static class Refresh extends AuthEvent {

        private final Profile profile;

        public Refresh(Throwable error, Profile profile) {
            super (error);
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }
    }

    public static class Logout extends AuthEvent {

        private final Profile profile;

        public Logout(Throwable error, Profile profile) {
            super (error);
            this.profile = profile;
        }

        public Profile getProfile() {
            return profile;
        }
    }

}
