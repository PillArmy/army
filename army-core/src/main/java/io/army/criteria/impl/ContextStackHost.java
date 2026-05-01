package io.army.criteria.impl;

import io.army.criteria.Item;
import io.army.lang.Nullable;

/// Statement context stack host object.
/// When Java garbage collects the host object, the context stack will be cleared.
/// @see ContextStack#push(Item, CriteriaContext)
/// @see java.lang.ref.Cleaner#register(Object, Runnable)
interface ContextStackHost {


    interface ContextStackHostHolder {

        @Nullable
        ContextStackHost getStackHost();

    }

}
