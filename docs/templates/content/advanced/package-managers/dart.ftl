# Dart Support

${solution_name} has two detectors for Dart:

* Dart Pub Spec Lock detector
* Dart Pub Dep detector

Both detectors will run if they find the following files:

* pubspec.yaml
* pubspeck.lock

If ${solution_name} cannot find a pubspec.lock file, but it finds a pubspec.yaml file, it will prompt the user to run the 'pub get' command to generate the pubspec.lock file, and then run ${solution_name} again.

The Dart Pub Spec Lock detector parses the pubspec.lock file for dependency information.  Because the file does not indicate relationships between components, results from this detector will be less accurate than those from the Dart Pub Dep detector.

The Dart Pub Dep detector runs the command 'pub deps' (which requires a pubspec.lock file to be present), and then parses the command's output for dependency information.  The detector will first try to run the command using a dart executable (if found), but if it is unsuccessful because the target project requires the Flutter SDK then it will try using a flutter executable (if found).

You may specify the location of dart and flutter executables using the [detect.dart.path](../../../properties/detectors/dart/#dart-executable) and [detect.flutter.path](../../../properties/detectors/dart/#flutter-executable) properties, respectively.

If you wish to exclude dev dependencies, you may do so using the [detect.pub.deps.exclude.dev](../../../properties/detectors/dart/#detect-dart-pub-deps-exclude-dev-dependencies-advanced) property, which will cause the detector to pass the --no-dev option when running 'pub deps'.