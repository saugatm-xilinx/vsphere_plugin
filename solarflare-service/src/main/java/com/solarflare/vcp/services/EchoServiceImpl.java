package com.solarflare.vcp.services;

/**
 * Implementation of the EchoService interface
 */
public class EchoServiceImpl implements EchoService {

   public String echo(String message) {
      return message;
   }
}
