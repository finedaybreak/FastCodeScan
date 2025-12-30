// ==========================================
// 文件: ScanScreen.kt
// 功能: 扫描二维码/条形码的主界面
// 说明: 使用 Jetpack Compose 构建 UI，集成 CameraX 实现相机预览和扫码功能
// ==========================================

// 声明当前文件所属的包（类似于文件夹路径）
package com.wongyichen.fastcodescan.presentation.scan

// ==========================================
// 导入区域 - 引入代码中需要用到的类和函数
// ==========================================

// Android 系统相关
import android.content.ClipData           // 剪贴板数据类，用于复制文本
import android.content.ClipboardManager   // 剪贴板管理器，用于访问系统剪贴板
import android.content.Context            // 上下文对象，用于访问系统服务和资源
import android.content.Intent             // 意图对象，用于启动其他应用（如浏览器）
import android.widget.Toast               // 短暂提示消息（屏幕底部的小弹窗）

// CameraX 相机库相关
import androidx.camera.core.CameraSelector     // 相机选择器，用于选择前置/后置摄像头
import androidx.camera.core.ImageAnalysis      // 图像分析器，用于处理相机帧进行扫码
import androidx.camera.core.Preview            // 相机预览，用于在屏幕上显示相机画面
import androidx.camera.lifecycle.ProcessCameraProvider  // 相机提供者，管理相机生命周期
import androidx.camera.view.PreviewView        // 预览视图，用于显示相机画面的 View

// Jetpack Compose UI 组件
import androidx.compose.foundation.background  // 设置背景颜色
import androidx.compose.foundation.layout.Arrangement  // 布局排列方式
import androidx.compose.foundation.layout.Box          // 盒子布局（可叠加子元素）
import androidx.compose.foundation.layout.Column       // 垂直排列的列布局
import androidx.compose.foundation.layout.Row          // 水平排列的行布局
import androidx.compose.foundation.layout.Spacer       // 空白间隔组件
import androidx.compose.foundation.layout.fillMaxSize  // 填充父容器的全部大小
import androidx.compose.foundation.layout.fillMaxWidth // 填充父容器的全部宽度
import androidx.compose.foundation.layout.height       // 设置高度
import androidx.compose.foundation.layout.padding      // 设置内边距
import androidx.compose.foundation.layout.size         // 设置固定大小
import androidx.compose.foundation.shape.RoundedCornerShape  // 圆角形状

// Material Design 3 图标和组件
import androidx.compose.material.icons.Icons                    // 图标集合
import androidx.compose.material.icons.outlined.ContentCopy     // 复制图标
import androidx.compose.material.icons.outlined.Language        // 网页/语言图标
import androidx.compose.material.icons.outlined.QrCodeScanner   // 二维码扫描图标
import androidx.compose.material3.Icon                          // 图标组件
import androidx.compose.material3.IconButton                    // 图标按钮组件
import androidx.compose.material3.MaterialTheme                 // Material Design 主题
import androidx.compose.material3.Text                          // 文本组件

// Compose 运行时相关
import androidx.compose.runtime.Composable        // 标记可组合函数的注解
import androidx.compose.runtime.DisposableEffect  // 带清理逻辑的副作用
import androidx.compose.runtime.LaunchedEffect    // 启动协程的副作用
import androidx.compose.runtime.collectAsState    // 将 Flow 转换为 Compose State
import androidx.compose.runtime.getValue          // 委托属性获取值
import androidx.compose.runtime.mutableStateOf    // 创建可变状态
import androidx.compose.runtime.remember          // 记住值，避免重复创建
import androidx.compose.runtime.rememberUpdatedState  // 保持回调函数最新引用
import androidx.compose.runtime.setValue          // 委托属性设置值

// 生命周期相关
import androidx.lifecycle.Lifecycle                // 生命周期状态枚举
import androidx.lifecycle.LifecycleEventObserver  // 生命周期事件观察者

// Compose UI 相关
import androidx.compose.ui.Alignment              // 对齐方式
import androidx.compose.ui.Modifier               // 修饰符，用于设置组件属性
import androidx.compose.ui.draw.clip              // 裁剪形状
import androidx.compose.ui.graphics.Color         // 颜色类
import androidx.compose.ui.platform.LocalContext  // 获取当前 Context
import androidx.lifecycle.compose.LocalLifecycleOwner  // 获取当前生命周期所有者
import androidx.compose.ui.text.style.TextAlign   // 文本对齐方式
import androidx.compose.ui.unit.dp                // dp 单位（设备无关像素）
import androidx.compose.ui.viewinterop.AndroidView  // 在 Compose 中嵌入传统 Android View
import androidx.compose.ui.window.Dialog          // 对话框组件

// Android 核心工具
import androidx.core.content.ContextCompat        // 兼容性工具，用于获取主线程执行器

// Hilt 依赖注入
import androidx.hilt.navigation.compose.hiltViewModel  // 获取 Hilt 注入的 ViewModel

// Accompanist 权限库（Google 提供的 Compose 扩展库）
import com.google.accompanist.permissions.ExperimentalPermissionsApi  // 实验性权限 API
import com.google.accompanist.permissions.isGranted                    // 检查权限是否已授予
import com.google.accompanist.permissions.rememberPermissionState      // 记住权限状态

// 项目内部类
import com.wongyichen.fastcodescan.scanner.BarcodeAnalyzer  // 条码分析器
import com.wongyichen.fastcodescan.ui.components.AppButton  // 自定义按钮组件
import com.wongyichen.fastcodescan.ui.components.AppCard    // 自定义卡片组件
import com.wongyichen.fastcodescan.ui.components.ButtonVariant  // 按钮样式变体

// Java 并发工具
import java.util.concurrent.Executors  // 线程池工厂，用于创建执行器

// Kotlin 扩展
import androidx.core.net.toUri  // 将字符串转换为 Uri 的扩展函数


// ==========================================
// 主扫描界面
// ==========================================

/**
 * 扫描界面的主入口函数
 *
 * @OptIn 注解: 表示使用了实验性的权限 API
 * @Composable 注解: 标记这是一个可组合函数，可以被 Compose 框架调用来构建 UI
 *
 * @param viewModel 扫描界面的 ViewModel，默认通过 Hilt 依赖注入获取
 *                  ViewModel 负责管理界面状态和业务逻辑
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel()  // 使用 Hilt 自动注入 ViewModel
) {
    // ========== 状态获取 ==========

    // 从 ViewModel 收集 UI 状态
    // collectAsState() 将 StateFlow 转换为 Compose 可观察的 State
    // by 关键字是 Kotlin 的委托属性，让我们可以直接使用 uiState 而不是 uiState.value
    val uiState by viewModel.uiState.collectAsState()

    // 获取当前的 Context（上下文），用于访问系统服务
    val context = LocalContext.current

    // ========== 权限管理 ==========

    // 创建相机权限状态管理器
    // rememberPermissionState 会记住权限状态，并在权限变化时触发重组
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    // LaunchedEffect: 当组件首次进入组合时执行的副作用
    // Unit 作为 key，表示只在首次组合时执行一次
    LaunchedEffect(Unit) {
        // 检查相机权限是否已授予
        if (!cameraPermissionState.status.isGranted) {
            // 如果未授予，则请求权限（会弹出系统权限对话框）
            cameraPermissionState.launchPermissionRequest()
        }
    }

    // ========== 主界面布局 ==========

    // Box: 盒子布局，子元素可以叠加在一起
    // fillMaxSize(): 填充父容器的全部空间
    Box(modifier = Modifier.fillMaxSize()) {

        // 根据权限状态显示不同内容
        if (cameraPermissionState.status.isGranted) {
            // ========== 权限已授予：显示相机预览 ==========

            // 获取当前的生命周期所有者（通常是 Activity 或 Fragment）
            // 用于将相机绑定到生命周期，实现自动启停
            val lifecycleOwner = LocalLifecycleOwner.current

            // 【修复】使用 rememberUpdatedState 保持回调函数始终指向最新的 viewModel 方法
            // 这样即使重组，回调也能正确调用最新的 ViewModel 方法
            val currentOnBarcodeScanned by rememberUpdatedState(viewModel::onBarcodeScanned)

            // 创建条码分析器实例
            // remember: 记住这个对象，避免每次重组都创建新实例
            // 【修复】回调使用 rememberUpdatedState 包装的引用，确保始终调用最新的方法
            val analyzer = remember {
                BarcodeAnalyzer { result ->
                    currentOnBarcodeScanned(result)  // 使用包装后的回调，确保引用最新
                }
            }

            // ========== 扫描状态同步 ==========

            // 【修复】使用 LaunchedEffect 监听 isScanning 状态变化
            // 确保 analyzer 的扫描状态与 ViewModel 保持同步
            LaunchedEffect(uiState.isScanning) {
                if (uiState.isScanning) {
                    // 正在扫描：恢复分析器工作
                    analyzer.resumeScanning()
                } else {
                    // 暂停扫描：暂停分析器（比如显示结果对话框时）
                    analyzer.pauseScanning()
                }
            }

            // ========== 生命周期事件处理 ==========

            // 【修复】监听生命周期事件，处理应用前后台切换
            // 使用 lifecycleOwner 作为 key，当 Activity 重建时会重新注册观察者
            DisposableEffect(lifecycleOwner) {
                // 创建生命周期事件观察者
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        // 当应用从后台恢复（ON_RESUME）时
                        Lifecycle.Event.ON_RESUME -> {
                            // 【修复】只有在 ViewModel 状态为扫描中时才恢复
                            // 避免在显示对话框时错误恢复扫描
                            if (uiState.isScanning) {
                                analyzer.resumeScanning()
                            }
                        }
                        // 当应用进入后台（ON_PAUSE）时
                        Lifecycle.Event.ON_PAUSE -> {
                            // 【修复】暂停扫描，避免后台继续消耗资源
                            analyzer.pauseScanning()
                        }
                        else -> { /* 其他事件不处理 */ }
                    }
                }
                // 将观察者添加到生命周期
                lifecycleOwner.lifecycle.addObserver(observer)

                // onDispose: 清理代码，在 Effect 结束时执行
                onDispose {
                    // 移除观察者，防止内存泄漏
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // ========== 相机预览组件 ==========

            // 显示相机预览画面
            // 【修复】将 isScanning 状态传递给 CameraPreview，用于控制相机重新绑定
            CameraPreview(
                analyzer = analyzer,
                isScanning = uiState.isScanning,
                modifier = Modifier.fillMaxSize()
            )

            // ========== 扫描框覆盖层 ==========

            // 在相机预览上叠加扫描框
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center  // 内容居中对齐
            ) {
                // 扫描框容器
                Box(
                    modifier = Modifier
                        .size(280.dp)                      // 设置扫描框大小为 280dp x 280dp
                        .clip(RoundedCornerShape(8.dp))    // 裁剪为 8dp 圆角
                        .background(Color.Transparent)     // 背景透明
                ) {
                    // 绘制扫描框四个角的装饰线条
                    ScanFrameCorners()
                }
            }

            // ========== 底部提示文字 ==========

            Column(
                modifier = Modifier
                    .fillMaxWidth()                    // 宽度填充父容器
                    .align(Alignment.BottomCenter)     // 对齐到底部中央
                    .padding(bottom = 100.dp),         // 底部内边距 100dp
                horizontalAlignment = Alignment.CenterHorizontally  // 子元素水平居中
            ) {
                // 显示提示文字
                Text(
                    text = "Align QR code or barcode within the frame",  // 提示用户对准扫描框
                    style = MaterialTheme.typography.bodyMedium,  // 使用中等正文字体样式
                    color = Color.White,                          // 白色文字
                    textAlign = TextAlign.Center                  // 文字居中
                )
            }
        } else {
            // ========== 权限未授予：显示权限请求界面 ==========

            Column(
                modifier = Modifier
                    .fillMaxSize()           // 填充整个屏幕
                    .padding(24.dp),         // 四周内边距 24dp
                horizontalAlignment = Alignment.CenterHorizontally,  // 水平居中
                verticalArrangement = Arrangement.Center             // 垂直居中
            ) {
                // 显示二维码扫描图标
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,  // 使用轮廓风格的扫描图标
                    contentDescription = null,                    // 无障碍描述（装饰性图标可为空）
                    modifier = Modifier.size(64.dp),              // 图标大小 64dp
                    tint = MaterialTheme.colorScheme.onSurfaceVariant  // 使用主题的次要表面颜色
                )

                // 添加 16dp 的垂直间距
                Spacer(modifier = Modifier.height(16.dp))

                // 标题文字
                Text(
                    text = "Camera Permission Required",  // "需要相机权限"
                    style = MaterialTheme.typography.titleMedium  // 中等标题字体
                )

                // 添加 8dp 的垂直间距
                Spacer(modifier = Modifier.height(8.dp))

                // 说明文字
                Text(
                    text = "Please grant camera permission to scan QR codes and barcodes",  // 请授予相机权限以扫描二维码和条形码
                    style = MaterialTheme.typography.bodyMedium,       // 中等正文字体
                    color = MaterialTheme.colorScheme.onSurfaceVariant,  // 次要颜色
                    textAlign = TextAlign.Center                       // 居中对齐
                )

                // 添加 24dp 的垂直间距
                Spacer(modifier = Modifier.height(24.dp))

                // 授权按钮
                AppButton(
                    text = "Grant Permission",                                   // 按钮文字
                    onClick = { cameraPermissionState.launchPermissionRequest() }  // 点击时请求权限
                )
            }
        }

        // ========== 扫描结果对话框 ==========

        // 当需要显示结果对话框且有扫描结果时
        if (uiState.showResultDialog && uiState.scanResult != null) {
            ScanResultDialog(
                content = uiState.scanResult!!.content,  // 扫描内容（!! 是非空断言）
                format = uiState.scanResult!!.codeFormat.name.replace("_", " "),  // 码格式，将下划线替换为空格
                onDismiss = { viewModel.dismissResult() },   // 关闭对话框的回调
                onContinue = { viewModel.resumeScanning() }, // 继续扫描的回调
                context = context                            // 传入 Context
            )
        }
    }
}


// ==========================================
// 相机预览组件
// ==========================================

/**
 * 相机预览组件
 *
 * 【修复说明】：
 * - 使用 isCameraBound 状态防止重复绑定相机
 * - 将相机初始化移到 DisposableEffect 中，只在首次进入和恢复时执行
 * - 添加 isScanning 参数用于在需要时触发相机重新绑定
 *
 * @param analyzer 条码分析器，用于分析相机帧
 * @param isScanning 是否正在扫描，用于触发相机重新绑定
 * @param modifier 修饰符，用于自定义组件外观和行为
 */
@Composable
private fun CameraPreview(
    analyzer: BarcodeAnalyzer,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    // 获取当前 Context
    val context = LocalContext.current
    // 获取当前生命周期所有者
    val lifecycleOwner = LocalLifecycleOwner.current

    // 创建单线程执行器，用于在后台线程处理图像分析
    // remember: 记住这个执行器，避免重复创建
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // 【修复】使用状态跟踪相机是否已绑定，防止重复绑定
    var isCameraBound by remember { mutableStateOf(false) }

    // 【修复】记住 PreviewView 实例，避免重复创建
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // 【修复】封装相机绑定逻辑为可复用函数
    // 使用 remember 包装以避免每次重组都创建新的 lambda
    val bindCamera = remember(lifecycleOwner, analyzer) {
        {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    // 创建预览用例
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    // 创建图像分析用例
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, analyzer)
                        }

                    // 选择后置摄像头
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // 先解绑所有已绑定的用例，再重新绑定
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    isCameraBound = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    isCameraBound = false
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // 【修复】使用 DisposableEffect 管理相机生命周期
    // 只在组件首次进入组合时绑定相机，离开时解绑
    DisposableEffect(lifecycleOwner) {
        // 初始绑定相机
        bindCamera()

        onDispose {
            // 组件销毁时解绑相机并释放资源
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        cameraProviderFuture.get().unbindAll()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isCameraBound = false
        }
    }

    // 【修复】监听生命周期恢复事件，重新绑定相机
    // 这解决了切换导航后相机不工作的问题
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 【修复】应用恢复时重新绑定相机
                    if (!isCameraBound) {
                        bindCamera()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    // 标记相机需要重新绑定
                    isCameraBound = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 【修复】当扫描状态变为 true 且相机未绑定时，重新绑定相机
    // 这解决了从结果对话框返回后相机不工作的问题
    LaunchedEffect(isScanning) {
        if (isScanning && !isCameraBound) {
            bindCamera()
        }
    }

    // 组件销毁时关闭执行器
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // ========== 显示 PreviewView ==========

    // 【修复】AndroidView 的 update 不再包含相机绑定逻辑
    // 只负责显示 PreviewView，避免重组时重复绑定
    AndroidView(
        factory = { previewView },
        modifier = modifier,
        update = { /* 不再在这里绑定相机 */ }
    )
}


// ==========================================
// 扫描框四角装饰组件
// ==========================================

/**
 * 绘制扫描框四个角的白色装饰线条
 *
 * 视觉效果：
 *  ┌──          ──┐
 *  │              │
 *
 *  │              │
 *  └──          ──┘
 */
@Composable
private fun ScanFrameCorners() {
    // 定义角落装饰的样式参数
    val cornerColor = Color.White      // 角落颜色：白色
    val cornerSize = 40.dp             // 角落大小：40dp
    val cornerWidth = 4.dp             // 线条宽度：4dp

    // 外层 Box 填充整个父容器
    Box(modifier = Modifier.fillMaxSize()) {

        // ========== 左上角 ==========
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)   // 对齐到左上角
                .size(cornerSize)            // 设置大小
        ) {
            // 水平线条（顶部横线）
            Box(
                modifier = Modifier
                    .fillMaxWidth()          // 宽度填充
                    .height(cornerWidth)     // 高度为线条宽度
                    .background(cornerColor) // 设置背景色
            )
            // 垂直线条（左侧竖线）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = cornerWidth)  // 顶部留出横线的空间
            ) {
                Box(
                    modifier = Modifier
                        .size(cornerWidth, cornerSize - cornerWidth)  // 宽度为线宽，高度为总高减去横线高度
                        .background(cornerColor)
                )
            }
        }

        // ========== 右上角 ==========
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)     // 对齐到右上角
                .size(cornerSize)
        ) {
            // 水平线条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .background(cornerColor)
            )
            // 垂直线条
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = cornerWidth)
            ) {
                Box(
                    modifier = Modifier
                        .size(cornerWidth, cornerSize - cornerWidth)
                        .background(cornerColor)
                        .align(Alignment.TopEnd)  // 对齐到右侧
                )
            }
        }

        // ========== 左下角 ==========
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)  // 对齐到左下角
                .size(cornerSize)
        ) {
            // 水平线条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .align(Alignment.BottomStart)  // 对齐到底部
                    .background(cornerColor)
            )
            // 垂直线条
            Box(
                modifier = Modifier
                    .size(cornerWidth, cornerSize - cornerWidth)
                    .background(cornerColor)
            )
        }

        // ========== 右下角 ==========
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)    // 对齐到右下角
                .size(cornerSize)
        ) {
            // 水平线条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .align(Alignment.BottomEnd)  // 对齐到底部
                    .background(cornerColor)
            )
            // 垂直线条
            Box(
                modifier = Modifier
                    .size(cornerWidth, cornerSize - cornerWidth)
                    .background(cornerColor)
                    .align(Alignment.TopEnd)     // 对齐到右上（因为在右下角的 Box 里）
            )
        }
    }
}


// ==========================================
// 扫描结果对话框组件
// ==========================================

/**
 * 显示扫描结果的对话框
 *
 * @param content 扫描到的内容文本
 * @param format 码的格式（如 QR_CODE、EAN_13 等）
 * @param onDismiss 关闭对话框时的回调
 * @param onContinue 点击"继续"按钮时的回调
 * @param context Android Context，用于访问系统服务
 */
@Composable
private fun ScanResultDialog(
    content: String,
    format: String,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    context: Context
) {
    // 判断扫描内容是否为 URL（网址）
    // startsWith: 检查字符串是否以指定前缀开头
    val isUrl = content.startsWith("http://") || content.startsWith("https://")

    // Dialog: Compose 的对话框组件
    // onDismissRequest: 当用户点击对话框外部或按返回键时调用
    Dialog(onDismissRequest = onDismiss) {
        // 使用自定义的卡片组件作为对话框内容容器
        AppCard {
            // 垂直布局
            Column(
                modifier = Modifier.padding(8.dp)  // 内边距 8dp
            ) {
                // ========== 标题 ==========
                Text(
                    text = "Scan Result",                         // 标题文字
                    style = MaterialTheme.typography.titleLarge   // 大标题字体样式
                )

                // 小间距
                Spacer(modifier = Modifier.height(4.dp))

                // ========== 码格式标签 ==========
                Text(
                    text = format,                                    // 显示码的格式
                    style = MaterialTheme.typography.labelSmall,      // 小标签字体
                    color = MaterialTheme.colorScheme.onSurfaceVariant  // 次要颜色
                )

                // 间距
                Spacer(modifier = Modifier.height(16.dp))

                // ========== 扫描内容显示区域 ==========
                Text(
                    text = content,                                   // 扫描到的内容
                    style = MaterialTheme.typography.bodyMedium,      // 中等正文字体
                    modifier = Modifier
                        .fillMaxWidth()                               // 宽度填充
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,  // 设置背景色
                            RoundedCornerShape(8.dp)                   // 8dp 圆角
                        )
                        .padding(12.dp)                               // 内边距 12dp
                )

                // 间距
                Spacer(modifier = Modifier.height(16.dp))

                // ========== 底部操作按钮区域 ==========
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)  // 子元素间距 8dp
                ) {
                    // 复制按钮
                    IconButton(
                        onClick = {
                            // 获取系统剪贴板服务
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            // 将扫描内容复制到剪贴板
                            clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Content", content))
                            // 显示 Toast 提示
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        // 复制图标
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy"  // 无障碍描述
                        )
                    }

                    // 如果内容是 URL，显示"在浏览器中打开"按钮
                    if (isUrl) {
                        IconButton(
                            onClick = {
                                // 创建打开网页的 Intent
                                // toUri(): 将字符串转换为 Uri
                                // ACTION_VIEW: 查看数据的通用动作
                                val intent = Intent(Intent.ACTION_VIEW, content.toUri())
                                // 启动浏览器
                                context.startActivity(intent)
                            }
                        ) {
                            // 网页图标
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Open URL"
                            )
                        }
                    }

                    // 弹性空白，将后面的按钮推到右边
                    // weight(1f): 占据所有剩余空间
                    Spacer(modifier = Modifier.weight(1f))

                    // 继续扫描按钮
                    AppButton(
                        text = "Continue",                    // 按钮文字
                        onClick = onContinue,                 // 点击回调
                        variant = ButtonVariant.Primary       // 主要按钮样式
                    )
                }
            }
        }
    }
}
