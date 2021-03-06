/** Copyright 2016 VMware, Inc. All rights reserved. -- VMware Confidential */

package com.solarflare.vcp.services;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solarflare.vcp.ModelObjectUriResolver;
import com.solarflare.vcp.model.Chassis;
import com.solarflare.vcp.model.ChassisInfo;

/**
 * Implements actions defined in ChassisService
 *
 */
public class ChassisServiceImpl implements ChassisService {
   private static final Log _logger = LogFactory.getLog(ChassisServiceImpl.class);

   private final ModelObjectUriResolver _uriResolver;
   private final ObjectStore _objectStore;

   /**
    * Constructor.
    *
    * @param uriResolver
    *    Custom type resolver for ModelObject resources used in this sample
    *
    * @param objectStore The in-memory store used for this sample.
    */
   public ChassisServiceImpl(
            ModelObjectUriResolver uriResolver,
            ObjectStore objectStore) {
      _uriResolver = uriResolver;
      _objectStore = objectStore;
   }

   //------------------------------------------------------------------------------------
   // ChassisService methods.

   @Override
   public URI createChassis(ChassisInfo chassisInfo) {
      Chassis chassis = _objectStore.createChassis(chassisInfo, true);

      if (chassis == null) {
         _logger.info("Chassis not created because this name is taken: " + chassisInfo.name);
         return null;
      }
      _logger.info("Chassis created: " + chassis.toString());

      // Important: the returned object is the reference generated by the reference service
      // not the chassis object itself!  In practice it will be a URI that get mapped into
      // a IResourceReference in the UI layer.
      return chassis.getUri(_uriResolver);
   }

   @Override
   public boolean updateChassis(URI chassisRef, ChassisInfo newInfo) {
      String uid = _uriResolver.getUid(chassisRef);

      Chassis chassis = _objectStore.getChassis(uid);
      if (chassis == null) {
         // Chassis may have already been deleted by another user
         _logger.info("Chassis not found during edit operation: " + uid);
         return false;
      }

      return _objectStore.replaceChassis(uid, newInfo);
   }

   @Override
   public boolean deleteChassis(URI chassisRef) {
      String uid = _uriResolver.getUid(chassisRef);

      Chassis chassis = _objectStore.removeChassis(uid);
      if (chassis == null) {
         // Chassis may have already been deleted by another user
         _logger.info("Chassis not found during delete operation: " + uid);
         return false;
      }
      return true;
   }

}
