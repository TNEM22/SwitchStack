const express = require('express');
const espController = require('../controllers/esp');
const authController = require('../controllers/auth');

const router = express.Router();

router
  .route('/')
  .get(authController.protect, espController.getAllEsp)
  .post(espController.createEsp);
router.route('/:id').patch(authController.protect, espController.updateEsp);

// Qr code process
router
  .route('/switch')
  .get(authController.protect, espController.getAllSwitch)
  .post(authController.protect, espController.createSwitches);
router
  .route('/switch/:id')
  .patch(authController.protect, espController.updateSwitch);

module.exports = router;
