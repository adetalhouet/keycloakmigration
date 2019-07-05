# Keycloakmigration

This projects aims to automate the configuration of the keycloak authentication provider.
It is inspired by the database migration tool liquibase.
The goal is to provide a similar mechanism for Keycloak. There also exists a gradle plugin for automated build pipelines: [keycloakmigrationplugin](https://github.com/klg71/keycloakmigrationplugin).

# Usage
Then migration can simply be invoked through the jar.

    java -jar keycloakmigration.jar --help

    usage: [-h] [-u USER] [-p PASSWORD] [-b BASEURL] [MIGRATION-FILE] [-r REALM]
           [-c CLIENT]

    optional arguments:
      -h, --help            show this help message and exit

      -u USER, --user USER  Username for the migration user, defaulting to admin.

      -p PASSWORD,          Password for the migration user, defaulting to admin.
      --password PASSWORD

      -b BASEURL,           Base url of keycloak server, defaulting to
      --baseurl BASEURL     http://localhost:18080/auth.

      -r REALM,             Realm to use for migration, defaulting to master
      --realm REALM

      -c CLIENT,            Client to use for migration, defaulting to master
      --client CLIENT


# Migration Details
Migrations are controlled through the changelog. It contains the changeSets used to execute the migration.

(Currently are file references are only allowed in the same directory)

    
    includes:
      - path: 01_initial.yml
      - path: 02_second.yml

A changeset may then look like this:

    id: initial-keycloak
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        emailVerified: true
        attributes:
          test:
          - test
          - test2
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas

The migration hashes are stored in the attribute named 'migration' in the migration user.

There are no transactions in keycloak though if the rollback fails there might be a non deterministic state.
If it fails I would like to receive a bug report for this.

# Supported migrations
This are the currently implemented commands. I hope I can find the time to implement more of them.
## User Migrations
### addUser
#### Descriptions
Adds a user to keycloak. Fails if a user with that name already exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
- enabled: Boolean,  default=true
- emailVerified: Boolean,  default=true
- attributes: Map with Layout Map<String, List<String>>, default=empty
#### Example
    id: test
    author: klg71
    changes:
    - addUser:
        realm: master
        name: test
        enabled: true
        emailVerified: true
        attributes:
          test:
          - test
          - test2

### deleteUser
#### Descriptions
Removes a user from keycloak. Fails if a user with that name does not exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
#### Example
    id: test
    author: klg71
    changes:
    - deleteUser:
        realm: master
        name: test
### updateUser
Updates an exiting user in keycloak. Fails if no user with given name exists.
#### Parameters
- realm: String, not optional
- name: String, not optional
- enabled: Boolean, default=no change
- emailVerified: Boolean, default=no change
- access: String, default=no change
- notBefore: Long, default=no change
- totp: Boolean, default=no change
- disableableCredentialTypes: List<String>, default=no change
- requiredActions: List<String>, default=no change
- email: String, default=no change
- firstName: String, default=no change
- lastName: String, default=no change
#### Example
    id: test
    author: klg71
    changes:
    - updateUser:
        realm: master
        name: test
        enabled: false
        lastName: Lukas

### addUserAttribute
Adds an attribute to an existing user. Throws an error if the user does not exist.

User attributes can't be set deterministic with the updateUser action.
#### Parameters
- realm: String, not optional
- name: String, not optional
- attributeName: String, not optional
- attributeValues: List<String>, not optional
- override: Boolean, default=false

#### Example
    id: test
    author: klg71
    changes:
    - addUserAttribute:
        realm: master
        name: test
        attributeName: test1
        attributeValues:
        - value1
        - value2

### deleteUserAttribute
Deletes an attribute to an existing user. Throws an error if the user does not exist.
#### Parameters
- realm: String, not optional
- name: String, not optional
- attributeName: String, not optional
- failOnMissing: Boolean, default=true

#### Example
    id: test
    author: klg71
    changes:
    - deleteUserAttribute:
        realm: master
        name: test
        attributeName: test1

### assignRole
Assigns a role to the given user. Fails if the user or the role doesn't exist.
#### Parameters
- realm: String, not optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - assignRole:
        realm: master
        user: testUser
        role: testRole

### revokeRole
Revokes a role from the given user. Fails if the user or the role doesn't exist or the user does not have the role assigned.

#### Parameters
- realm: String, not optional
- user: String, not optional
- role: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - revokeRole:
        realm: master
        user: testUser
        role: testRole

## Group Migrations
### addGroup
Adds a new group to keycloak. Fails if the group already exists.

#### Parameters
- realm: String, not optional
- name: String, not optional
- parent: String, default=empty

#### Example
    id: test
    author: klg71
    changes:
    - addGroup:
        realm: master
        name: testUser

### deleteGroup
Removes a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, not optional
- name: String, not optional

#### Example
    id: test
    author: klg71
    changes:
    - deleteGroup:
        realm: master
        name: testUser

### updateGroup
Updates a group from keycloak. Fails if the group does not exist.

#### Parameters
- realm: String, not optional
- name: String, not optional
- attributes: Map<String,List<String>>, optional, default=existing attributes
- realmRoles: List<String>, optional, default=existing realm roles
- clientRoles: Map<String,List<String>>, optional, default=existing client roles

#### Example
    id: test
    author: klg71
    changes:
      - updateGroup:
          realm: master
          name: child1
          attributes:
            lkz:
              - "1234"
        

## Role Migrations
### addRole
### deleteRole

## Client Migrations
### addSimpleClient
### deleteClient
### importClient
Imports a client using the json representation.

#### Parameters
- realm: String, not optional
- clientRepresentationJsonFilename: String, not optional

#### Example
    id: import-client
    author: klg71
    changes:
    - importClient:
          realm: master
          clientRepresentationJsonFilename: client.json

## User Federation Migrations
### AddAdLdap

# Hacking
To start developing on this project you can use the gradle tasks.

To start the local development keycloak you can just use the task ```startLocalKeycloak```

# TODOS:
- Add more commands
- Add gradle plugin
- Add sophisticated unit and integration Tests
- Test Keycloak with PostgreSQL instead of H2 backend.
- Add token refresh logic
- Allow relative file references
