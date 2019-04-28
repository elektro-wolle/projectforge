package org.projectforge.rest.dto

import org.projectforge.framework.persistence.user.entities.PFUserDO

class User(var username: String? = null,
           var firstname: String? = null,
           var lastname: String? = null,
           var description: String? = null,
           var email: String? = null
) : BaseObject<PFUserDO>() {

    override fun copyFromMinimal(src: PFUserDO) {
        super.copyFromMinimal(src)
        if (src is PFUserDO) {
            username = src.username
        }
    }
}