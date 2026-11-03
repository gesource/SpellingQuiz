import Foundation
import MLKitCommon
import MLKitDigitalInkRecognition
import Observation

enum ModelState {
    case preparing, ready, failed
}

@Observable
final class SpellingRecognizer: @unchecked Sendable {
    private let model = DigitalInkRecognitionModel(modelIdentifier: .enUs)
    private let recognizer: DigitalInkRecognizer
    private let modelManager = ModelManager.modelManager()
    private var downloadObservers: [NSObjectProtocol] = []

    private(set) var modelState: ModelState = .preparing

    init() {
        recognizer = DigitalInkRecognizer.digitalInkRecognizer(options: DigitalInkRecognizerOptions(model: model))
    }

    func prepareModel() {
        if modelManager.isModelDownloaded(model) {
            modelState = .ready
            return
        }

        let center = NotificationCenter.default
        downloadObservers = [
            center.addObserver(forName: .mlkitModelDownloadDidSucceed, object: nil, queue: .main) { [weak self] _ in
                self?.finishDownload(state: .ready)
            },
            center.addObserver(forName: .mlkitModelDownloadDidFail, object: nil, queue: .main) { [weak self] _ in
                self?.finishDownload(state: .failed)
            },
        ]
        _ = modelManager.download(model, conditions: ModelDownloadConditions())
    }

    private func finishDownload(state: ModelState) {
        modelState = state
        let center = NotificationCenter.default
        downloadObservers.forEach { center.removeObserver($0) }
        downloadObservers.removeAll()
    }

    func recognize(
        ink: Ink,
        width: Float,
        height: Float,
        onResult: @escaping @Sendable ([String]) -> Void,
        onError: @escaping @Sendable (Error) -> Void
    ) {
        let context = DigitalInkRecognitionContext(preContext: "", writingArea: WritingArea(width: width, height: height))
        recognizer.recognize(ink: ink, context: context) { result, error in
            if let error {
                onError(error)
                return
            }
            onResult(result?.candidates.map(\.text) ?? [])
        }
    }
}

func matchesSpelling(_ candidates: [String], _ spelling: String) -> Bool {
    let answer = normalize(spelling)
    return candidates.prefix(5).contains { normalize($0) == answer }
}

private func normalize(_ text: String) -> String {
    text.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
}
