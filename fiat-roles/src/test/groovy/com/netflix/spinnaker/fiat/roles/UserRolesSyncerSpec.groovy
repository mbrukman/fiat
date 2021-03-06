/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.roles

import com.netflix.spinnaker.fiat.model.UserPermission
import com.netflix.spinnaker.fiat.model.resources.Account
import com.netflix.spinnaker.fiat.permissions.InMemoryPermissionsRepository
import com.netflix.spinnaker.fiat.permissions.PermissionsResolver
import spock.lang.Specification
import spock.lang.Subject

class UserRolesSyncerSpec extends Specification {

  def "should sync all user roles"() {
    setup:
      def permissionsRepo = new InMemoryPermissionsRepository();

      def user1 = new UserPermission()
          .setId("user1")
          .setAccounts([new Account().setName("account1")] as Set)
      def user2 = new UserPermission()
          .setId("user2")
          .setAccounts([new Account().setName("account2")] as Set)

      permissionsRepo.put("user1", user1)
      permissionsRepo.put("user2", user2)

      def newUser2 = new UserPermission()
          .setId("user2")
          .setAccounts([new Account().setName("account3")] as Set)


      def permissionsResolver = Mock(PermissionsResolver)
      @Subject syncer = new UserRolesSyncer()
          .setPermissionsRepository(permissionsRepo)
          .setPermissionsResolver(permissionsResolver)

    expect:
      permissionsRepo.get("user1") == user1
      permissionsRepo.get("user2") == user2

    when:
      syncer.sync()

    then:
      permissionsResolver.resolve(_ as Set) >> ["user1": user1, "user2": newUser2]

    expect:
      permissionsRepo.get("user1") == user1
      permissionsRepo.get("user2") == newUser2
  }
}
