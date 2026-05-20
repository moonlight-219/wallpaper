<template>
	<view class="login">
		<view class="back-btn" @click="goBack">
			<uni-icons type="left" size="20" color="#534747"></uni-icons>
		</view>
		<view class="content">
			<!-- Logo区域 -->
			<view class="logo-section">
				<view class="logo-container">
					<view class="logo-bg"></view>
					<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
				</view>
				<view class="app-name">壁纸精选</view>
				<view class="slogan">发现美好 · 点亮生活</view>
			</view>

			<!-- 登录区域 -->
			<view class="login-section">
				<!-- #ifdef H5 -->
				<!-- H5 模式切换 -->
				<view class="mode-switch">
					<text :class="['mode-item', { active: isLoginMode }]" @click="switchMode('login')">登录</text>
					<text :class="['mode-item', { active: !isLoginMode }]" @click="switchMode('register')">注册</text>
				</view>

				<!-- H5 登录表单 -->
				<view v-if="isLoginMode" class="form-container">
					<view class="input-group">
						<uni-icons type="person" size="20" color="#999"></uni-icons>
						<input class="input" type="text" v-model="loginForm.username" placeholder="请输入用户名"
							placeholder-class="input-placeholder" />
					</view>
					<view class="input-group">
						<uni-icons type="locked" size="20" color="#999"></uni-icons>
						<input class="input" :type="showPassword ? 'text' : 'password'" v-model="loginForm.password"
							placeholder="请输入密码" placeholder-class="input-placeholder" />
						<uni-icons :type="showPassword ? 'eye-slash' : 'eye'" size="20" color="#999"
							@click="togglePassword"></uni-icons>
					</view>
					<button class="login-btn" :loading="loading" @click="handlePasswordLogin">
						{{ loading ? '登录中...' : '登录' }}
					</button>
				</view>

				<!-- H5 注册表单 -->
				<view v-else class="form-container">
					<view class="input-group">
						<uni-icons type="person" size="20" color="#999"></uni-icons>
						<input class="input" type="text" v-model="registerForm.username" placeholder="请输入用户名"
							placeholder-class="input-placeholder" />
					</view>
					<view class="input-group">
						<uni-icons type="locked" size="20" color="#999"></uni-icons>
						<input class="input" :type="showPassword ? 'text' : 'password'" v-model="registerForm.password"
							placeholder="请输入密码（至少6位）" placeholder-class="input-placeholder" />
						<uni-icons :type="showPassword ? 'eye-slash' : 'eye'" size="20" color="#999"
							@click="togglePassword"></uni-icons>
					</view>
					<view class="input-group">
						<uni-icons type="locked" size="20" color="#999"></uni-icons>
						<input class="input" :type="showConfirmPassword ? 'text' : 'password'" v-model="registerForm.confirmPassword"
							placeholder="请确认密码" placeholder-class="input-placeholder" />
						<uni-icons :type="showConfirmPassword ? 'eye-slash' : 'eye'" size="20" color="#999"
							@click="toggleConfirmPassword"></uni-icons>
					</view>
					<button class="login-btn register-btn" :loading="loading" @click="handleRegister">
						{{ loading ? '注册中...' : '注册' }}
					</button>
				</view>
				<!-- #endif -->

				<!-- #ifdef MP-WEIXIN -->
				<!-- 微信小程序授权登录 -->
				<view class="wechat-form">
					<view class="login-tip">
						<text class="tip-text">使用微信快速登录</text>
					</view>
					<button class="wechat-btn" :loading="loading" @click="handleWechatLogin">
						<uni-icons type="weixin" size="40" color="#fff"></uni-icons>
						<text class="btn-text">{{ loading ? '登录中...' : '微信一键登录' }}</text>
					</button>
				</view>
				<!-- #endif -->
			</view>

			<!-- 协议 -->
			<view class="agreement">
				<checkbox-group @change="handleAgreement">
					<label class="agreement-label">
						<checkbox value="agree" :checked="agreed" color="#7a544d" style="transform: scale(0.75);" />
						<text class="text">
							登录即表示同意
							<text class="link" @click.stop="showAgreement('user')">《用户协议》</text>
							和
							<text class="link" @click.stop="showAgreement('privacy')">《隐私政策》</text>
						</text>
					</label>
				</checkbox-group>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { login, register } from '@/apis/user.js'
import { uploadImage } from '@/apis/upload.js'
import { useUserStore } from '@/stores/user.js'
import { finishLoginNavigation } from '@/utils/auth.js'
import { safeNavigateBack } from '@/utils/navigation.js'

const { setToken, setUserInfo } = useUserStore()

const redirectAfterLogin = ref('')

onLoad((options) => {
	if (options.redirect) {
		try {
			redirectAfterLogin.value = decodeURIComponent(options.redirect)
		} catch {
			redirectAfterLogin.value = options.redirect
		}
	}
	if (!redirectAfterLogin.value) {
		const cached = uni.getStorageSync('authRedirect')
		if (cached) redirectAfterLogin.value = cached
	}
})

onShow(() => {
	if (!redirectAfterLogin.value) {
		const cached = uni.getStorageSync('authRedirect')
		if (cached) redirectAfterLogin.value = cached
	}
})

const goAfterLogin = () => {
	finishLoginNavigation(redirectAfterLogin.value)
}

const goBack = () => {
	safeNavigateBack()
}

const agreed = ref(true)
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const tempAvatar = ref('')
const tempNickname = ref('')
const isLoginMode = ref(true) // H5端登录/注册模式切换
const loginForm = ref({
	username: '',
	password: ''
})
const registerForm = ref({
	username: '',
	password: '',
	confirmPassword: ''
})

const togglePassword = () => {
	showPassword.value = !showPassword.value
}

const toggleConfirmPassword = () => {
	showConfirmPassword.value = !showConfirmPassword.value
}

const switchMode = (mode) => {
	isLoginMode.value = mode === 'login'
}

const handleAgreement = (e) => {
	agreed.value = e.detail.value.length > 0
}

const onChooseAvatar = (e) => {
	const { avatarUrl } = e.detail
	if (avatarUrl) {
		tempAvatar.value = avatarUrl
		console.log('选择头像:', avatarUrl)
	}
}

const handlePasswordLogin = async () => {
	if (!agreed.value) {
		uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
		return
	}

	if (!loginForm.value.username.trim()) {
		uni.showToast({ title: '请输入用户名', icon: 'none' })
		return
	}

	if (!loginForm.value.password.trim()) {
		uni.showToast({ title: '请输入密码', icon: 'none' })
		return
	}

	loading.value = true
	try {
		const res = await login({
			username: loginForm.value.username,
			password: loginForm.value.password
		})

		if (res.code === 200) {
			setToken(res.data.token)
			uni.setStorageSync('userId', res.data.userId)

			setUserInfo({
				id: res.data.userId,
				username: res.data.username,
				nickname: res.data.nickname,
				avatar: res.data.avatar,
				role: res.data.role,
				isCreator: res.data.isCreator,
				followerCount: res.data.followerCount,
				workCount: res.data.workCount,
				bio: res.data.bio
			})

			uni.showToast({ title: '登录成功', icon: 'success' })
			setTimeout(() => {
				goAfterLogin()
			}, 800)
		}
	} catch (error) {
		console.error('登录失败:', error)
	} finally {
		loading.value = false
	}
}

const handleRegister = async () => {
	if (!agreed.value) {
		uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
		return
	}

	if (!registerForm.value.username.trim()) {
		uni.showToast({ title: '请输入用户名', icon: 'none' })
		return
	}

	if (registerForm.value.username.trim().length < 3) {
		uni.showToast({ title: '用户名至少3个字符', icon: 'none' })
		return
	}

	if (!registerForm.value.password.trim()) {
		uni.showToast({ title: '请输入密码', icon: 'none' })
		return
	}

	if (registerForm.value.password.length < 6) {
		uni.showToast({ title: '密码至少6位', icon: 'none' })
		return
	}

	if (registerForm.value.password !== registerForm.value.confirmPassword) {
		uni.showToast({ title: '两次密码不一致', icon: 'none' })
		return
	}

	loading.value = true
	try {
		const res = await register({
			username: registerForm.value.username,
			password: registerForm.value.password
		})

		if (res.code === 200) {
			uni.showToast({ title: '注册成功，请登录', icon: 'success' })

			setTimeout(() => {
				isLoginMode.value = true
				loginForm.value.username = registerForm.value.username
				loginForm.value.password = ''
				registerForm.value = { username: '', password: '', confirmPassword: '' }
			}, 1500)
		}
	} catch (error) {
		console.error('注册失败:', error)
	} finally {
		loading.value = false
	}
}

const handleWechatLogin = async () => {
	if (!agreed.value) {
		uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
		return
	}

	// #ifdef MP-WEIXIN
	loading.value = true
	try {
		// 1. 获取微信登录凭证
		uni.showLoading({ title: '登录中...' })
		const loginRes = await new Promise((resolve, reject) => {
			uni.login({
				provider: 'weixin',
				success: (res) => resolve(res),
				fail: (err) => reject(err)
			})
		})

		console.log('uni.login 结果:', loginRes)

		if (!loginRes.code) {
			uni.showToast({ title: '获取登录凭证失败，请确保已登录微信', icon: 'none', duration: 2000 })
			return
		}

		// 2. 调用登录接口，不传头像和昵称
		const res = await login({
			wechatOpenId: loginRes.code
		})

		console.log('登录响应:', res)

		if (res.code === 200 && res.data) {
			console.log('登录成功，token:', res.data.token ? '已获取' : '未获取')

			setToken(res.data.token)
			uni.setStorageSync('userId', res.data.userId)

			setUserInfo({
				id: res.data.userId,
				username: res.data.username,
				nickname: res.data.nickname || '微信用户',
				avatar: res.data.avatar || '',
				role: res.data.role,
				isCreator: res.data.isCreator,
				followerCount: res.data.followerCount,
				workCount: res.data.workCount,
				bio: res.data.bio
			})

			uni.showToast({ title: '登录成功', icon: 'success' })
			setTimeout(() => {
				goAfterLogin()
			}, 800)
		} else {
			console.error('登录失败，响应:', res)
			uni.showToast({ title: res.message || '登录失败', icon: 'none' })
		}
	} catch (error) {
		console.error('微信登录失败:', error)
		uni.showToast({ title: error.message || '登录失败，请重试', icon: 'none' })
	} finally {
		uni.hideLoading()
		loading.value = false
	}
	// #endif
}

const showAgreement = (type) => {
	const title = type === 'user' ? '用户协议' : '隐私政策'
	uni.showToast({ title: `查看${title}`, icon: 'none' })
}
</script>

<style lang="scss" scoped>
.login {
	min-height: 100vh;
	width: 100vw;
	background: linear-gradient(180deg, #fff5f4 0%, #ffffff 30%, #fef5f4 100%);
	display: flex;
	flex-direction: column;
	position: relative;

	.back-btn {
		position: absolute;
		top: 40rpx;
		left: 30rpx;
		width: 70rpx;
		height: 70rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background: rgba(255, 255, 255, 0.8);
		border-radius: 50%;
		z-index: 100;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);

		&:active {
			background: rgba(255, 255, 255, 0.95);
		}
	}

	.content {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
		padding: 120rpx 60rpx 80rpx;

		// #ifdef H5
		padding: 40rpx 60rpx 80rpx;
		// #endif

		.logo-section {
			text-align: center;
			flex: 1;
			display: flex;
			flex-direction: column;
			justify-content: center;
			padding-bottom: 100rpx;

			.logo-container {
				width: 340rpx;
				height: 340rpx;
				margin: 0 auto 60rpx;
				position: relative;
				display: flex;
				align-items: center;
				justify-content: center;

				.logo-bg {
					display: none;
				}

				.logo {
					width: 340rpx;
					height: 340rpx;
					position: relative;
					z-index: 1;
				}
			}

			.app-name {
				font-size: 68rpx;
				font-weight: 700;
				color: #613942;
				margin-bottom: 28rpx;
				letter-spacing: 6rpx;
				text-shadow: 0 2rpx 8rpx rgba(97, 57, 66, 0.08);
			}

			.slogan {
				font-size: 30rpx;
				color: #999;
				letter-spacing: 3rpx;
			}
		}

		.login-section {
			// #ifdef H5
			.mode-switch {
				display: flex;
				justify-content: center;
				align-items: center;
				gap: 60rpx;
				margin-bottom: 50rpx;

				.mode-item {
					font-size: 34rpx;
					color: #999;
					padding: 16rpx 40rpx;
					border-radius: 40rpx;
					transition: all 0.3s ease;

					&.active {
						color: #fff;
						background: linear-gradient(135deg, #7a544d 0%, #613942 100%);
						font-weight: 600;
						box-shadow: 0 8rpx 24rpx rgba(122, 84, 77, 0.3);
					}
				}
			}
			// #endif

			.welcome {
				font-size: 40rpx;
				font-weight: 600;
				color: #613942;
				text-align: center;
				margin-bottom: 60rpx;
			}

			.form-container {
				.input-group {
					display: flex;
					align-items: center;
					background: #fff;
					border-radius: 55rpx;
					padding: 0 40rpx;
					height: 110rpx;
					margin-bottom: 30rpx;
					box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);

					.input {
						flex: 1;
						font-size: 30rpx;
						color: #333;
						margin-left: 20rpx;

						&.input-placeholder {
							color: #999;
						}
					}
				}

				.login-btn {
				width: 100%;
				height: 110rpx;
				background: linear-gradient(135deg, #7a544d 0%, #613942 100%);
				color: #fff;
				border-radius: 55rpx;
				border: none;
				box-shadow: 0 16rpx 48rpx rgba(122, 84, 77, 0.4);
				font-size: 36rpx;
				font-weight: 600;
				line-height: 110rpx;
				padding: 0;

				&::after {
					border: none;
				}

				&.register-btn {
					background: linear-gradient(135deg, #5a8f7b 0%, #4a7569 100%);
					box-shadow: 0 16rpx 48rpx rgba(90, 143, 123, 0.4);
				}
			}
			}

			.wechat-form {
				.login-tip {
					text-align: center;
					margin-bottom: 40rpx;

					.tip-text {
						font-size: 28rpx;
						color: #666;
					}
				}

				.wechat-btn {
					width: 100%;
					height: 110rpx;
					background: linear-gradient(135deg, #09bb07 0%, #07c160 100%);
					color: #fff;
					border-radius: 55rpx;
					border: none;
					box-shadow: 0 16rpx 48rpx rgba(9, 187, 7, 0.4);
					display: flex;
					flex-direction: row;
					align-items: center;
					justify-content: center;
					gap: 20rpx;
					padding: 0;
					line-height: 110rpx;

					&::after {
						border: none;
					}

					.btn-text {
						font-size: 36rpx;
						font-weight: 600;
						line-height: 110rpx;
					}
				}
			}
		}

		.agreement {
			margin-top: 60rpx;

			.agreement-label {
				display: flex;
				align-items: center;
				justify-content: center;
				gap: 12rpx;

				.text {
					font-size: 24rpx;
					color: #999;
					line-height: 1.8;

					.link {
						color: #7a544d;
						font-weight: 500;
					}
				}
			}
		}
	}
}
</style>
