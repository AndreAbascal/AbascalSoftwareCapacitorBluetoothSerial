
  Pod::Spec.new do |s|
    s.name = 'AbascalSoftwareCapacitorBluetoothSerial'
    s.version = '0.0.1'
    s.summary = 'This is the Capacitor version for the cordova-plugin-bluetooth-serial'
    s.license = 'MIT'
    s.homepage = 'https://github.com/AndreAbascal/AbascalSoftwareCapacitorBluetoothSerial.git'
    s.author = 'Abascal Software'
    s.source = { :git => 'https://github.com/AndreAbascal/AbascalSoftwareCapacitorBluetoothSerial.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end