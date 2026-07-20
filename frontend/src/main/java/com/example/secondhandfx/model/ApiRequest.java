package com.example.secondhandfx.model;

/**
 * Marker interface for all client-side request DTOs sent as the body
 * of a POST/PUT/PATCH call via HttpClientHelper.
 * Implementing this interface is required so the compiler prevents
 * passing arbitrary/raw objects (e.g. Map, String) as a request body.
 */
public interface ApiRequest {
}