

import React, { Component } from 'react';
import { requireNativeComponent } from 'react-native';

const RNTakePicture = requireNativeComponent('RNTakePicture', RNTakePictureView);

export default class RNTakePictureView extends Component {
  cameraRef = React.createRef();

  setRef = ref => (this.caRef = ref);

  render() {
    return <RNTakePicture ref={this.cameraRef} {...this.props} />;
  }
}
