const Esp = require('../models/esp');
const Switch = require('../models/switch');
const AppError = require('../utils/appError');
const catchAsync = require('../utils/catchAsync');

exports.getAllEsp = catchAsync(async (req, res, next) => {
  const esps = await Esp.find({
    user: req.user.id,
  });

  res.status(200).json({
    status: 'success',
    results: esps.length,
    data: {
      esps,
    },
  });
});

exports.getAllSwitch = catchAsync(async (req, res, next) => {
  let esp = await Esp.find({ user: req.user._id });
  const switches = esp[0].switches;
  let states = [];

  states.push(+(await Switch.findById(switches[0])).state);
  states.push(+(await Switch.findById(switches[1])).state);
  states.push(+(await Switch.findById(switches[2])).state);
  states.push(+(await Switch.findById(switches[3])).state);

  // switches.forEach(async (sw) => {
  //   let aSwitch = await Switch.findById(sw);
  //   states.push(aSwitch.state);
  // });

  res.status(200).json({
    status: 'success',
    results: switches.length,
    data: {
      switches,
    },
    states: {
      states,
    },
  });
});

exports.createEsp = catchAsync(async (req, res, next) => {
  const newEsp = await Esp.create(req.body);

  res.status(201).json({
    status: 'success',
    data: {
      esp: newEsp,
    },
  });
});

// Qr code process
exports.createSwitches = catchAsync(async (req, res, next) => {
  let switches = [];
  const esp = await Esp.findOneAndUpdate(
    { id: req.body.id },
    {
      user: req.user.id,
    },
  );
  for (let i = 0; i < esp.noOfSwitches; i += 1) {
    switches.push({ esp: esp._id });
  }
  const newSwitches = await Switch.insertMany(switches);
  switches = [];
  newSwitches.forEach((sw) => {
    switches.push(sw._id);
  });
  await Esp.findByIdAndUpdate(esp._id, {
    switches: switches,
  });
  res.status(201).json({
    status: 'success',
    data: {
      switches: newSwitches,
    },
  });
});

exports.updateEsp = catchAsync(async (req, res, next) => {
  const esp = await Esp.findByIdAndUpdate(req.params.id, req.body);

  if (!esp) {
    return next(new AppError(`No esp found with ${req.params.id} ID`, 404));
  }

  res.status(200).json({
    status: 'success',
    // data: {
    //   esp: esp,
    // },
  });
});

exports.updateSwitch = catchAsync(async (req, res, next) => {
  const aSwitch = await Switch.findByIdAndUpdate(req.params.id, req.body);

  if (!aSwitch) {
    return next(new AppError(`No switch found with ${req.params.id} ID`, 404));
  }

  res.status(200).json({
    status: 'success',
    // data: {
    //   switch: aSwitch,
    // },
  });
});
