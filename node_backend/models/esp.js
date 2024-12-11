const mongoose = require('mongoose');

const espSchema = new mongoose.Schema({
  id: {
    type: String,
    trim: true,
    unique: true,
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
  },
  name: {
    type: String,
    default: 'Room',
    trim: true,
  },
  active: {
    type: Boolean,
    default: false,
  },
  noOfSwitches: {
    type: Number,
    required: [true, 'Number of switches must be given.'],
  },
  switches: {
    type: [mongoose.Schema.Types.ObjectId],
    ref: 'Switch',
  },
});

const Esp = mongoose.model('Esp', espSchema);
module.exports = Esp;
