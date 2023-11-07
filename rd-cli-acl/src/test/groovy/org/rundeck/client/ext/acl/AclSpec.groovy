package org.rundeck.client.ext.acl

import com.dtolabs.rundeck.core.authorization.ValidationSet
import com.dtolabs.rundeck.core.authorization.providers.YamlProvider
import spock.lang.Specification

class AclSpec extends Specification {
    def String TEST_YAML1 = """
description: Admin, all access.
context:
  project: '.*' # all projects
for:
  resource:
    - allow: '*' # allow read/create all kinds
  adhoc:
    - allow: '*' # allow read/running/killing adhoc jobs
  job: 
    - allow: '*' # allow read/write/delete/run/kill of all jobs
  node:
    - allow: '*' # allow read/run for all nodes
by:
  group: admin

---

description: Admin, all access.
context:
  application: 'rundeck'
for:
  resource:
    - allow: '*' # allow create of projects
  project:
    - allow: '*' # allow view/admin of all projects
  project_acl:
    - allow: '*' # allow admin of all project-level ACL policies
  storage:
    - allow: '*' # allow read/create/update/delete for all /keys/* storage content
by:
  group: admin
"""

    def "test basic yaml validate"() {
        given:
            def validationSet = new ValidationSet()
            def validation =
                YamlProvider.validate(
                    YamlProvider.sourceFromString('testacl1', TEST_YAML1, new Date(), validationSet),
                    validationSet
                );
        expect:
            validation.valid
    }
}
