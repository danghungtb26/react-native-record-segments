import React, { Component } from 'react'
import { requireNativeComponent, findNodeHandle, NativeModules } from 'react-native'
import Proptypes from 'prop-types'

const RecordManager = NativeModules.RNDVCamera

class CameraView extends Component {
  constructor(props) {
    super(props)
    this.state = {}
    this.camera = React.createRef()
  }

  findNode = () => {
    return findNodeHandle(this.camera.current)
  }

  flashChange = () => {
    RecordManager.changeFlash(this.findNode())
  }

  switchChange = () => {
    RecordManager.changeSwitch(this.findNode())
  }

  record = () => {
    RecordManager.record(this.findNode())
  }

  pause = () => {
    RecordManager.pause(this.findNode())
  }

  resume = () => {
    RecordManager.resume(this.findNode())
  }

  done = () => {
    RecordManager.done(this.findNode())
  }

  capture = () => {
    RecordManager.done(this.findNode())
  }

  checkInitOrDone = () => {
    return RecordManager.isInitOrDone(this.findNode())
  }

  isRecording = () => {
    return RecordManager.isRecording(this.findNode())
  }

  onDVAfterUpdate = e => {
    const { onDVAfterUpdate } = this.props
    onDVAfterUpdate(e.nativeEvent)
  }

  onDVCameraReady = () => {
    const { onDVCameraReady } = this.props
    onDVCameraReady()
  }

  onDoneStart = () => {
    const { onDoneStart } = this.props
    onDoneStart()
  }

  onDoneSuccess = e => {
    const { onDoneSuccess } = this.props
    onDoneSuccess(e.nativeEvent)
  }

  onDVProgress = e => {
    const { onDVProgress } = this.props
    onDVProgress(e.nativeEvent)
  }

  render() {
    return (
      <RNRecord
        ref={this.camera}
        {...this.props}
        onDoneStart={this.onDoneStart}
        onDoneSuccess={this.onDoneSuccess}
        onDVCameraReady={this.onDVCameraReady}
        onDVProgress={this.onDVProgress}
        onDVAfterUpdate={this.onDVAfterUpdate}
      />
    )
  }
}

const RNRecord = requireNativeComponent('RNDVCamera', CameraView)

CameraView.propTypes = {
  onDVAfterUpdate: Proptypes.func,
  onDoneStart: Proptypes.func,
  onDoneSuccess: Proptypes.func,

  onDVProgress: Proptypes.func,
  onDVCameraReady: Proptypes.func,
}

CameraView.defaultProps = {
  onDVAfterUpdate: () => {},
  onDoneStart: () => {},
  onDoneSuccess: () => {},
  onDVProgress: () => {},
  onDVCameraReady: () => {}
}

export default CameraView
