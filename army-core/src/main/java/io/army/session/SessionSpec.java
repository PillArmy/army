package io.army.session;

import io.army.criteria.Visible;
import io.army.spec.OptionSpec;

/// This interface is base interface of {@link Session} for pass to {@link io.army.dialect.DialectParser}.
public interface SessionSpec extends OptionSpec {


    /// Get the name of session.
    /// **NOTE** : This method don't check whether session closed or not.
    String name();


    /// Get the visible mode(soft delete) of session.
    /// **NOTE** : This method don't check whether session closed or not.
    Visible visible();

}
