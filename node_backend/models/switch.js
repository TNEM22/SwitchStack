const mongoose = require('mongoose');

const switchSchema = new mongoose.Schema({
  esp: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Esp',
    required: [true, 'A switch must consist of a Esp.'],
  },
  name: {
    type: String,
    default: 'Switch',
    trim: true,
  },
  state: {
    type: Boolean,
    default: false,
  },
});

const Switch = mongoose.model('Switch', switchSchema);
module.exports = Switch;
