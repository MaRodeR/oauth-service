package org.bp.labs.oauthservice.services.exceptions;

import java.io.Serializable;

public interface HasDetails {

    public Serializable getErrorDetails();
}
