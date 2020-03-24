

import React, { Component } from 'react';
import { requireNativeComponent } from 'react-native';

const RNRecord = requireNativeComponent('RNRecord', RNRecordView);

export default class RNRecordView extends Component {
  cameraRef = React.createRef();

  setRef = ref => (this.caRef = ref);

  render() {
    return <RNRecord ref={this.cameraRef} {...this.props} />;
  }
}
