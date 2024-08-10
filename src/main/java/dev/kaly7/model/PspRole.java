package dev.kaly7.model;
/**
 * Enum representing different Payment Service Provider (PSP) roles.
 * PSP roles define the types of services that a payment service provider can offer
 * within the context of financial transactions and banking services.
 */
public enum PspRole {

    /**
     * Payment Initiation Service Provider (PISP) - A service provider that initiates
     * payment transactions on behalf of a customer.
     */
    PISP,

    /**
     * Account Information Service Provider (AISP) - A service provider that provides
     * account information services, such as retrieving account balance and transaction history.
     */
    AISP,

    /**
     * Payment Initiation and Information Service Provider (PIISP) - A service provider
     * that combines both payment initiation and account information services.
     */
    PIISP,

    /**
     * Account Servicing Payment Service Provider (ASPSP) - A service provider that
     * holds and manages customer accounts and facilitates payment transactions.
     */
    ASPSP
}
