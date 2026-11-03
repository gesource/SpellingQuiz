import MLKitDigitalInkRecognition
import SwiftUI

/// 手書きの1点。座標はパッド内の pt、t は UNIX エポックからのミリ秒。
struct InkPoint {
    let x: CGFloat
    let y: CGFloat
    let t: Double
}

struct HandwritingPad: View {
    @Binding var strokes: [[InkPoint]]
    let enabled: Bool
    let onSizeChange: (CGSize) -> Void

    @State private var isNewStroke = true

    var body: some View {
        GeometryReader { geometry in
            Canvas { context, _ in
                for points in strokes {
                    draw(points: points, in: &context)
                }
            }
            .background(Color.white)
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        guard enabled else { return }
                        let point = InkPoint(
                            x: value.location.x,
                            y: value.location.y,
                            t: value.time.timeIntervalSince1970 * 1000
                        )
                        if isNewStroke {
                            strokes.append([point])
                            isNewStroke = false
                        } else {
                            strokes[strokes.count - 1].append(point)
                        }
                    }
                    .onEnded { _ in
                        isNewStroke = true
                    }
            )
            .onAppear { onSizeChange(geometry.size) }
            .onChange(of: geometry.size) { onSizeChange(geometry.size) }
        }
    }

    private func draw(points: [InkPoint], in context: inout GraphicsContext) {
        guard let first = points.first else { return }
        let style = StrokeStyle(lineWidth: 6, lineCap: .round, lineJoin: .round)
        if points.count == 1 {
            let radius = style.lineWidth / 2
            let rect = CGRect(x: first.x - radius, y: first.y - radius, width: radius * 2, height: radius * 2)
            context.fill(Path(ellipseIn: rect), with: .color(.black))
            return
        }
        var path = Path()
        path.move(to: CGPoint(x: first.x, y: first.y))
        for point in points.dropFirst() {
            path.addLine(to: CGPoint(x: point.x, y: point.y))
        }
        context.stroke(path, with: .color(.black), style: style)
    }
}

func toInk(_ strokes: [[InkPoint]]) -> Ink {
    let mlStrokes = strokes.map { points in
        Stroke(points: points.map { StrokePoint(x: Float($0.x), y: Float($0.y), t: Int($0.t)) })
    }
    return Ink(strokes: mlStrokes)
}
