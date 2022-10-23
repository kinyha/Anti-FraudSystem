# Anti-FraudSystem
## About
This project demonstrates (in a simplified form) the principles of anti-fraud systems in the financial sector. For this project, we will work on a system with an expanded role model, a set of REST endpoints responsible for interacting with users, and an internal transaction validation logic based on a set of heuristic rules.
##### Description

To begin with, let's define the concepts and find out what makes a good anti-fraud system. Consider the procedure of online payment (a transaction):

![](https://ucarecdn.com/77957b67-fdd9-4c87-9c1b-aaa4d29ef382/)

Frauds carry significant financial costs and risks for all stakeholders. So, the presence of an anti-fraud system is a necessity for any serious e-commerce platform.

Let's implement a simple anti-fraud system consisting of one rule — **heuristics**. In the beginning, there's one simple measure that prevents fraudsters from illegally transferring money from an account. Suppose some scammers acquired access to confidential financial information through **phishing** or **pharming**. They immediately try to transfer as much as possible. Most of the time, the account holder is not aware of the attack. The anti-fraud system should prevent it before it is too late.

In the first stage, you need to create a simple rest endpoint that calculates whether a transaction is `ALLOWED`, `PROHIBITED`, or requires `MANUAL_PROCESSING` by evaluating the amount of the transaction.

Enterprise applications like anti-fraud systems are used by different types of users with various access levels. Different users should have different rights to access various system parts. Let's set up the authentication procedure for our system. Of course, you can elaborate it yourself, but it is considered good practice to use an already tested and reliable implementation. Fortunately, Spring includes the Spring Security module that contains the right methods.

In this stage, you need to provide the HTTP Basic authentication for our `REST` service with the `JDBC` implementations of `UserDetailService` for user management. You will require an endpoint for registering users at `POST /api/auth/user`.

An enterprise anti-fraud system has hundreds of merchant users who take advantage of the system by checking the validity of the transactions only. These users do not want to delve into a list of stolen cards, suspicious IP addresses, and who else uses the app. On the other hand, the number of users responsible for reporting stolen cards/IPs and excluding them from the blacklist is limited. These users don't have access to user-management functions. Finally, we have several users who are 100%-trusted and are allowed to access and modify more sensitive data.

In this stage, you need to add the **authorization** feature. Authorization is a process when the system decides whether an authenticated client has permission to access the requested resource. Authorization always follows authentication.

Let's implement the role model for our system:

| | Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT
|:-------------------------|:---------:|:----:|:----------:|:-------------:|
| POST /api/auth/user | + | + | + | + |
| DELETE /api/auth/user | \- | \- | + | \- |
| GET /api/auth/list | \- | \- | + | + |
| POST /api/antifraud/transaction | \- | + | \- | \- |
| POST /api/antifraud/access | \- | \- | + | \- |

Let's talk about roles. `ADMINISTRATOR` is the user who has registered first, all other users should receive the `MERCHANT` roles. All users added after `ADMINISTRATOR` must be locked by default and unlocked later by `ADMINISTRATOR`. The `SUPPORT` role should be assigned by `ADMINISTRATOR` to one of the users later.

Usually, hackers use certain IP addresses. As a result, you should be cautious about activities coming from these addresses. In addition, card numbers can be reported as stolen. The anti-fraud system should prohibit all transactions with stolen cards.

In this stage, we need to enable our anti-fraud system to retrieve a list of prohibited card numbers and suspicious IP addresses to ban them from carrying out any transactions.

In our service, we will check IP addresses for compliance with IPv4. Any address following this format consists of four series of numbers from `0` to `255` separated by dots. Here is an example of a valid IP address: `132.245.4.216`

Card numbers must be checked according to the **Luhn** **algorithm**. During testing, we will use the following card format: `4000008449433403`.

*   The first six digits are the **Issuer Identification Number** (IIN). The first digit is the **Major Industry Identifier** (MII);
*   The seventh to second-to-last digits indicate the **customer account number**;
*   The last digit is the **check digit** (or checksum). It validates the credit card number using the Luhn algorithm.

So, 16 digits in total.

In the current implementation, our system is very basic; each transaction is considered in isolation from others. Let's add **correlation** to our fraud detection rules. Now, the result of the operation depends on other operations.

Let's enrich the transaction event with the world region and the transaction date. There is the table for world region codes:

| Code | Description |
|:-------------------------|:---------:|
| EAP | East Asia and Pacific |
| ECA | Europe and Central Asia |
| HIC | High-Income countries |
| LAC | Latin America and the Caribbean |
| MENA | The Middle East and North Africa |
| SA | South Asia |
| SSA | Sub-Saharan Africa |

When working on an anti-fraud system, it is necessary to consider that the environment of transactions is constantly changing. There are many factors like the country's economy, the behavior of fraudsters, and the number of transactions happening concurrently that influence what we can call fraud. It is necessary to add certain adaptation mechanisms to our service, such as **feedback**. Feedback will be carried out manually by a `SUPPORT` specialist for completed transactions. Based on the feedback results, we will change the limits of fraud detection algorithms following the special rules. Take a look at the table below that shows the logic of our feedback system:

Transaction Feedback →

Transaction Validity ↓

ALLOWED

MANUAL\_PROCESSING

PROHIBITED

ALLOWED

Exception

↓ max ALLOWED

↓ max ALLOWED

↓ max MANUAL

MANUAL\_PROCESSING

↑ max ALLOWED

Exception

↓ max MANUAL

PROHIBITED

↑ max ALLOWED

↑ max MANUAL

↑ max MANUAL

Exception

The formula for increasing the limit:

    new_limit = 0.8 * current_limit + 0.2 * value_from_transaction
    

The formula for decreasing the limit:

    new_limit = 0.8 * current_limit - 0.2 * value_from_transaction
    

If the new value is fractional, **round** **it** **up** (use `ceil`).

Let's take an example:

Current `max ALLOWED` limit is `200`. A transaction of `210` is validated for `MANUAL_PROCESSING`. The feedback has been received that a transaction with a value `210` is `ALLOWED`. Max `ALLOWED` value must be updated to `202`:

    202 = 0.8 * 200 + 0.2 * 210
    

Consider the following limitations for feedback:

*   For each transaction, only one feedback is allowed.
*   Feedback is provided only by users with `SUPPORT` role.

##### Objectives

*   Create and run a SpringBoot application on the `28852` port;
    
*   Create the `POST /api/antifraud/transaction` endpoint that accepts data in the JSON format:

```
    {
      "amount": <Long>
    }
```

*   Implement the following rules:

1.  Transactions with a sum of lower or equal to `200` are `ALLOWED`;
2.  Transactions with a sum of greater than `200` but lower or equal than `1500` require `MANUAL_PROCESSING`;
3.  Transactions with a sum of greater than `1500` are `PROHIBITED`.  
    
    ![](https://ucarecdn.com/177f034f-34b4-4e25-ab4f-bb5e485ba2a1/)
    

*   The transaction amount must be greater than `0`.
*   If the validation process was successful, the endpoint should respond with the status `HTTP OK` (`200`) and return the following JSON:

```    
    {
      "result": "<String>"
    }
```
*   In case of wrong data in the request, the endpoint should respond with the status `HTTP Bad Request` (`400`).

*   Add the Spring security to your project and configure the HTTP basic authentication;
*   For storing users and passwords, add a JDBC implementation of `UserDetailsService` with an H2 database. Usernames must be **case insensitive**;
*   Add the `POST /api/auth/user` endpoint. In this stage, It must be available to unauthorized users for registration and accept data in the JSON format:
```
    {
       "name": "<String value, not empty>",
       "username": "<String value, not empty>",
       "password": "<String value, not empty>"
    }
```
*   If a user has been successfully added, the endpoint must respond with the `HTTP CREATED` status (`201`) and the following body:
```
    {
       "id": <Long value, not empty>,
       "name": "<String value, not empty>",
       "username": "<String value, not empty>"
    }
```
*   If an attempt to register an existing user was a failure, the endpoint must respond with the `HTTP CONFLICT` status (`409`);
*   If a request contains wrong data, the endpoint must respond with the `BAD REQUEST` status (`400`);
*   Add the `GET /api/auth/list` endpoint. It must be available to all authorized users;
*   The endpoint must respond with the `HTTP OK` status (`200`) and the body with an array of objects representing the users sorted by ID in **ascending order**. Return an empty JSON array if there's no information:
```
    [
        {
            "id": <user1 id>,
            "name": "<user1 name>",
            "username": "<user1 username>"
        },
         ...
        {
            "id": <userN id>,
            "name": "<userN name>",
            "username": "<userN username>"
        }
    ]
```
*   Add the `DELETE /api/auth/user/{username}` endpoint, where `{username}` specifies the user that should be deleted. The endpoint must be available to all authorized users. The endpoint must delete the user and respond with the `HTTP OK` status (`200`) and the following body:
```
    {
       "username": "<username>",
       "status": "Deleted successfully!"
    }
```
*   If a user is not found, respond with the `HTTP Not Found` status (`404`);
*   Change the `POST /api/antifraud/transaction` endpoint; it must be available only to all authorized users.

*   Add authorization to the service and implement the role model shown in the table above. The first registered user should receive the `ADMINISTRATOR` role; the rest — `MERCHANT`. In case of authorization violation, respond with the `HTTP Forbidden` status (`403`). Mind that only one role can be assigned to a user;
*   All users, except `ADMINISTRATOR`, must be locked immediately after registration; only `ADMINISTRATOR` can unlock users;
*   Change the response for the `POST /api/auth/user` endpoint. It should respond with the `HTTP Created` status (`201`) and the body with the JSON object containing the information about a user. Add the `role` field in the response:
```
    {
       "id": <Long value, not empty>,
       "name": "<String value, not empty>",
       "username": "<String value, not empty>",
       "role": "<String value, not empty>"
    }
```
*   Change the response for the `GET /api/auth/list` endpoint. Add the `role` field in the response:
```
    [
        {
            "id": <user1 id>,
            "name": "<user1 name>",
            "username": "<user1 username>",
            "role": "<user1 role>"
        },
         ...
        {
            "id": <userN id>,
            "name": "<userN name>",
            "username": "<userN username>",
            "role": "<userN role>"
        }
    ]
```
*   Add the `PUT /api/auth/role` endpoint that changes user roles. It must accept the following JSON body:
```
    {
       "username": "<String value, not empty>",
       "role": "<String value, not empty>"
    }
```
If successful, respond with the `HTTP OK` status (`200`) and the body like this:
```
    {
       "id": <Long value, not empty>,
       "name": "<String value, not empty>",
       "username": "<String value, not empty>",
       "role": "<String value, not empty>"
    }
```
*   If a user is not found, respond with the `HTTP Not Found` status (`404`);
*   If a role is not `SUPPORT` or `MERCHANT`, respond with `HTTP Bad Request` status (`400`);
*   If you want to assign a role that has been already provided to a user, respond with the `HTTP Conflict` status (`409`);
*   Add the `PUT /api/auth/access` endpoint that locks/unlocks users. It accepts the following JSON body:
```
    {
       "username": "<String value, not empty>",
       "operation": "<[LOCK, UNLOCK]>"  // determines whether the user will be activated or deactivated
    }
```
If successful, respond with the `HTTP OK` status (`200`) and the following body:
```
    {
        "status": "User <username> <[locked, unlocked]>!"
    }
```
*   For safety reasons, `ADMINISTRATOR` cannot be blocked. In this case, respond with the `HTTP Bad Request` status (`400`);
*   If a user is not found, the endpoint must respond with `HTTP Not Found` status (`404`).

*   Change the role model:

| |Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT
|:-------------------------|:---------:|:----:|:----------:|:-------------:|
| POST api/auth/user | + | + | + | + |
| DELETE api/auth/user | \- | \- | + | \- |
| GET api/auth/list | \- | \- | + | + |
| POST api/antifraud/transaction | \- | + | \- | \- |
| POST, DELETE, GET api/antifraud/suspicious-ip | \- | \- | \- | + |
| POST, DELETE, GET api/antifraud/stolencard | \- | \- | \- | + |

*   Add the `POST /api/antifraud/suspicious-ip` endpoint that saves suspicious IP addresses to the database. It must accept the following JSON body:
```
    {
       "ip": "<String value, not empty>"
    }
```
If successful, respond with the `HTTP Created` status (`200`) and the body like this:
```
    {
       "id": "<Long value, not empty>",
       "ip": "<String value, not empty>"
    }
```
If an IP is already in the database, respond with the `HTTP Conflict` status (`409`).

If an IP address has the wrong format, respond with the `HTTP Bad Request` status (`400`).

*   Add the `DELETE /api/antifraud/suspicious-ip/{ip}` endpoint that deletes IP addresses from the database. If IP addresses are deleted successfully, respond with the `HTTP OK` status (`200`) and the body like this:
```
    {
       "status": "IP <ip address> successfully removed!"
    }
```
If an IP is not found in the database, respond with the `HTTP Not Found` status (`404`).

If an IP address has the wrong format (not following the Description section rules), respond with the `HTTP Bad Request` status (`400`)

*   Add the `GET /api/antifraud/suspicious-ip` endpoint that shows IP addresses in the database. Endpoint must respond with the `HTTP OK` status (`200`) and body with an array of JSON objects representing IP address sorted by ID in **ascending** order (or an empty array if the database is empty):
```
    [
        {
            "id": 1,
            "ip": "192.168.1.1"
        },
         ...
        {
            "id": 100,
            "ip": "192.168.1.254"
        }
    ]
```
or
```
    []
```
*   Add the `POST /api/antifraud/stolencard` endpoint that saves stolen card data in the database. It must accept the following JSON body:
```
    {
       "number": "<String value, not empty>"
    }
```
If successful, respond with the `HTTP Created` status (`200`) and the following body:
```
    {
       "id": "<Long value, not empty>",
       "number": "<String value, not empty>"
    }
```
If the card number is already in the database, respond with the `HTTP Conflict` status (`409`).

If a card number has the wrong format, respond with the `HTTP Bad Request` status (`400`).

*   Add the `DELETE /api/antifraud/stolencard/{number}` endpoint that deletes a card number from the database. If a card number has been deleted, respond with the `HTTP OK` status (`200`) and the body below:
```
    {
       "status": "Card <number> successfully removed!"
    }
```
If a card number is not found in the database, respond with the `HTTP Not Found` status (`404`).

If a card number follows the wrong format (look at the Description section), respond with `HTTP Bad Request` status (`400`).

*   Add the `GET /api/antifraud/stolencard` endpoint that shows card numbers stored in the database. The endpoint must respond with the `HTTP OK` status (`200`) and a body with an array of JSON objects that display the card numbers sorted by ID in **ascending** **order** (or an empty array if the database is empty):
```
    [
        {
            "id": 1,
            "number": "4000008449433403"
        },
         ...
        {
            "id": 100,
            "number": "4000009455296122"
        }
    ]
```
*   Change the `POST /api/antifraud/transaction`, now endpoint must accept data in the following JSON format:
```
    {
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": "<String value, not empty>"
    }
```
*   Implement the following rules in addition to the previous ones:

1.  If an IP address is in the blacklist, the transaction is `PROHIBITED`;
2.  If a card number is in the blacklist, the transaction is `PROHIBITED`.

*   If the validation process was successful, the endpoint should respond with the status `HTTP OK` (`200`) and return the following JSON:
```
    {
      "result": <String>,
      "info": <String>
    }
```
If the `result` is `ALLOWED`, the `info` field must be set to `none` (a string value).

In the case of `PROHIBITED` or `MANUAL_PROCESSING`, the `info` field must contain the reason for rejecting the transaction; the reason must be separated by `,` and sorted alphabetically. For example, `amount, card-number, ip`.

If a request contains wrong data, an IP address and a card number must be validated as described in the Description section, the endpoint should respond with the status `HTTP Bad Request` (`400`).

*   Implement the transaction history. Save ALL transactions, even PROHIBITED ones, in the database.
    
*   Change the `POST /api/antifraud/transaction`. Now, the endpoint must accept data in the following JSON format:
```
    {
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": "<String value, not empty>",
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss"
    }
```
*   Change the rules for reviewing a transaction. A transaction containing a card number is `PROHIBITED` if:

1.  There are transactions from more than 2 regions of the world other than the region of the transaction that is being verified in the last hour in the transaction history;
2.  There are transactions from more than 2 unique IP addresses other than the IP of the transaction that is being verified in the last hour in the transaction history.

*   A transaction containing a card number is sent for `MANUAL_PROCESSING` if:

1.  There are transactions from 2 regions of the world other than the region of the transaction that is being verified in the last hour in the transaction history;
2.  There are transactions from 2 unique IP addresses other than the IP of the transaction that is being verified in the last hour in the transaction history.

*   If the validation process was successful, the endpoint should respond with the status `HTTP OK` (`200`) and return the following JSON:
```
    {
      "result": <String>,
      "info": <String>
    }
```
If the `result` is `ALLOWED`, the `info` field must be set to `none`.

In the case of the `PROHIBITED` or `MANUAL_PROCESSING` result, the `info` field must contain the reason for rejecting the transaction. The reason must be separated by `,` and sorted alphabetically. For example, `amount, card-number, ip, ip-correlation, region-correlation`.

If a request contains wrong data, the region and date must be validated as described above, but the endpoint should respond with the status `HTTP Bad Request` (`400`).

*   Change the role model:

|  | Anonymous | MERCHANT | ADMINISTRATOR | SUPPORT |
|:-------------------------|:---------:|:----:|:----------:|:-------------:|
| POST /api/auth/user | + | + | + | + |
| DELETE /api/auth/user | \- | \- | + | \- |
| GET /api/auth/list | \- | \- | + | + |
| POST /api/antifraud/transaction | \- | + | \- | \- |
| POST /api/antifraud/suspicious-ip | \- | \- | \- | +
| POST /api/antifraud/stolencard | \- | \- | \- | + |
| GET /api/antifraud/history | \- | \- | \- | + |
| PUT /api/antifraud/transaction | \- | \- | \- | + |

*   Add the `PUT` method to the `/api/antifraud/transaction` endpoint that adds feedback for a transaction. It must accept the following JSON body:
```
    {
       "transactionId": <Long>,
       "feedback": "<String>"
    }
```
If successful, update the limits of transaction validation following the table above and respond with the `HTTP OK` status (`200`) and the following body:
```
    {
      "transactionId": <Long>,
      "amount": <Long>,
      "ip": "<String value, not empty>",
      "number": "<String value, not empty>",
      "region": "<String value, not empty>",
      "date": "yyyy-MM-ddTHH:mm:ss",
      "result": "<String>",
      "feedback": "<String>"
    }
```
If the feedback for a specified transaction is already in the database, respond with the `HTTP Conflict` status (`409`).

If the feedback has the wrong format (other than `ALLOWED`, `MANUAL_PROCESSING`, `PROHIBITED`), respond with the `HTTP Bad Request` status (`400`).

If the feedback throws an Exception following the table, respond with the `HTTP Unprocessable Entity` status (`422`).

If the transaction is not found in history, respond with the `HTTP Not Found` status (`404`).

*   Add the `GET /api/antifraud/history` endpoint that shows the transaction history. The endpoint must respond with the `HTTP OK` status (`200`) and a body with an array of JSON objects that represent transactions sorted by ID in **ascending** order (or an empty array if the history is empty):
```
    [
        {
          "transactionId": <Long>,
          "amount": <Long>,
          "ip": "<String value, not empty>",
          "number": "<String value, not empty>",
          "region": "<String value, not empty>",
          "date": "yyyy-MM-ddTHH:mm:ss",
          "result": "<String>",
          "feedback": "<String>"
        },
         ...
        {
          "transactionId": <Long>,
          "amount": <Long>,
          "ip": "<String value, not empty>",
          "number": "<String value, not empty>",
          "region": "<String value, not empty>",
          "date": "yyyy-MM-ddTHH:mm:ss",
          "result": "<String>",
          "feedback": "<String>"
        }
    ]
```
or
```
    []
```
*   Add the `GET /api/antifraud/history/{number}` endpoint that shows the transaction history for a specified card number. If transactions for a specified card number are found, respond with the `HTTP OK` status (`200`) and the following body:
```
    [
        {
          "transactionId": <Long>,
          "amount": <Long>,
          "ip": "<String value, not empty>",
          "number": number,
          "region": "<String value, not empty>",
          "date": "yyyy-MM-ddTHH:mm:ss",
          "result": "<String>",
          "feedback": "<String>"
        },
         ...
        {
          "transactionId": <Long>,
          "amount": <Long>,
          "ip": "<String value, not empty>",
          "number": number,
          "region": "<String value, not empty>",
          "date": "yyyy-MM-ddTHH:mm:ss",
          "result": "<String>",
          "feedback": "<String>"
        }
    ]
```
If transactions for a specified card number are not found, respond with the `HTTP Not Found` status (`404`).

If a card number doesn't follow the right format like in previous stages, respond with `HTTP Bad Request` status (`400`).
