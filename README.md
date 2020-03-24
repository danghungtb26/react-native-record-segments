# react-native-recorder-segments

## Getting started

`$ npm install react-native-recorder-segments --save`

### Mostly automatic installation

`$ react-native link react-native-recorder-segments`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-recorder-segments` and add `RecorderSegments.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRecorderSegments.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.RecorderSegmentsPackage;` to the imports at the top of the file
  - Add `new RecorderSegmentsPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-recorder-segments'
  	project(':react-native-recorder-segments').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-recorder-segments/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-recorder-segments')
  	```


## Usage
```javascript
import RecorderSegments from 'react-native-recorder-segments';

// TODO: What to do with the module?
RecorderSegments;
```
