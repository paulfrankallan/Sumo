import UIKit
import Shared

class AppDelegate: NSObject, UIApplicationDelegate {
    static var orientationLock = UIInterfaceOrientationMask.portrait

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        Koin_iosKt.doInitKoinIos()
        return true
    }
}
