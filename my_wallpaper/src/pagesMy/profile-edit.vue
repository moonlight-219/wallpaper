<template>
	<view class="profile-edit">
		<myTitleBar title="编辑资料" :showBack="true" :hideSearch="true"></myTitleBar>

		<view class="content">
			<!-- 头像 -->
			<view class="form-item">
				<view class="label">头像</view>
				<view class="avatar-section" @click="chooseAvatar">
					<!-- #ifdef MP-WEIXIN -->
					<button class="avatar-btn" open-type="chooseAvatar" @chooseavatar="onChooseAvatar">
						<image v-if="formData.avatar" :src="formData.avatar" class="avatar-img" mode="aspectFill"></image>
						<view v-else class="avatar-placeholder">
							<uni-icons type="camera-filled" size="40" color="#ccc"></uni-icons>
						</view>
					</button>
					<!-- #endif -->
					<!-- #ifndef MP-WEIXIN -->
					<image v-if="formData.avatar" :src="formData.avatar" class="avatar-img" mode="aspectFill"></image>
					<view v-else class="avatar-placeholder">
						<uni-icons type="camera-filled" size="40" color="#ccc"></uni-icons>
					</view>
					<!-- #endif -->
					<view class="avatar-tip">
						<uni-icons type="right" size="16" color="#999"></uni-icons>
					</view>
				</view>
			</view>

			<!-- 昵称 -->
			<view class="form-item">
				<view class="label">昵称</view>
				<view class="input-section">
					<!-- #ifdef MP-WEIXIN -->
					<input class="input" type="nickname" v-model="formData.nickname" placeholder="请输入昵称" maxlength="20" />
					<!-- #endif -->
					<!-- #ifndef MP-WEIXIN -->
					<input class="input" type="text" v-model="formData.nickname" placeholder="请输入昵称" maxlength="20" />
					<!-- #endif -->
				</view>
			</view>

			<!-- 个人简介 -->
			<view class="form-item bio-item">
				<view class="label">个人简介</view>
				<view class="textarea-section">
					<textarea class="textarea" v-model="formData.bio" placeholder="介绍一下自己吧" maxlength="15" :show-confirm-bar="false"></textarea>
					<view class="char-count">{{ formData.bio.length }}/15</view>
				</view>
			</view>

			<!-- 保存按钮 -->
			<button class="save-btn" :loading="saving" @click="handleSave">
				{{ saving ? '保存中...' : '保存' }}
			</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import myTitleBar from '@/components/myTitleBar.vue'
import { updateUserInfo } from '@/apis/user.js'
import { uploadImage } from '@/apis/upload.js'
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore()
const saving = ref(false)
const tempAvatarPath = ref('') // 临时头像路径

const formData = ref({
	avatar: '',
	nickname: '',
	bio: ''
})

onLoad(() => {
	loadUserInfo()
})

const loadUserInfo = () => {
	const userInfo = userStore.userInfo.value
	if (userInfo) {
		formData.value = {
			avatar: userInfo.avatar || '',
			nickname: userInfo.nickname || '',
			bio: userInfo.bio || ''
		}
	}
}

// 微信小程序选择头像
const onChooseAvatar = (e) => {
	const { avatarUrl } = e.detail
	if (avatarUrl) {
		tempAvatarPath.value = avatarUrl
		formData.value.avatar = avatarUrl
		console.log('选择头像:', avatarUrl)
	}
}

// 非小程序选择头像
const chooseAvatar = () => {
	// #ifndef MP-WEIXIN
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			const tempPath = res.tempFilePaths[0]
			tempAvatarPath.value = tempPath
			formData.value.avatar = tempPath
			console.log('选择头像:', tempPath)
		}
	})
	// #endif
}

const handleSave = async () => {
	if (!formData.value.nickname.trim()) {
		uni.showToast({ title: '请输入昵称', icon: 'none' })
		return
	}

	saving.value = true
	try {
		let avatarUrl = formData.value.avatar

		// 如果有新选择的头像，先上传
		if (tempAvatarPath.value) {
			uni.showLoading({ title: '上传头像中...' })
			const uploadRes = await uploadImage(tempAvatarPath.value, 3) // type=3 表示头像
			console.log('头像上传结果:', uploadRes)

			if (uploadRes.code === 200 && uploadRes.data && uploadRes.data.url) {
				avatarUrl = uploadRes.data.url
				console.log('头像上传成功，URL:', avatarUrl)
			} else {
				uni.hideLoading()
				uni.showToast({ title: '头像上传失败', icon: 'none' })
				return
			}
		}

		// 更新用户信息
		uni.showLoading({ title: '保存中...' })
		const res = await updateUserInfo({
			nickname: formData.value.nickname.trim(),
			avatar: avatarUrl,
			bio: formData.value.bio.trim()
		})

		console.log('更新用户信息响应:', res)

		if (res.code === 200) {
			// 更新本地用户信息
			const currentUserInfo = userStore.userInfo.value
			userStore.setUserInfo({
				...currentUserInfo,
				nickname: formData.value.nickname.trim(),
				avatar: avatarUrl,
				bio: formData.value.bio.trim()
			})

			uni.showToast({ title: '保存成功', icon: 'success' })
			setTimeout(() => {
				uni.navigateBack()
			}, 800)
		} else {
			uni.showToast({ title: res.message || '保存失败', icon: 'none' })
		}
	} catch (error) {
		console.error('保存失败:', error)
		uni.showToast({ title: error.message || '保存失败', icon: 'none' })
	} finally {
		uni.hideLoading()
		saving.value = false
	}
}
</script>

<style lang="scss" scoped>
.profile-edit {
	min-height: 100vh;
	background: $self-background-color;

	.content {
		padding: 30rpx;

		.form-item {
			background: #fff;
			border-radius: 20rpx;
			padding: 30rpx;
			margin-bottom: 20rpx;
			box-shadow: 0 4rpx 20rpx rgba(122, 84, 77, 0.06);

			.label {
				font-size: 28rpx;
				color: #613942;
				font-weight: 600;
				margin-bottom: 20rpx;
			}

			.avatar-section {
				display: flex;
				align-items: center;
				justify-content: space-between;

				.avatar-btn {
					padding: 0;
					background: transparent;
					border: none;
					width: 120rpx;
					height: 120rpx;

					&::after {
						border: none;
					}
				}

				.avatar-img {
					width: 120rpx;
					height: 120rpx;
					border-radius: 50%;
					border: 3rpx solid #e8dbcb;
				}

				.avatar-placeholder {
					width: 120rpx;
					height: 120rpx;
					border-radius: 50%;
					background: #f5f5f5;
					border: 3rpx dashed #ddd;
					display: flex;
					align-items: center;
					justify-content: center;
				}

				.avatar-tip {
					color: #999;
				}
			}

			.input-section {
				.input {
					width: 100%;
					height: 80rpx;
					background: #f8f8f8;
					border-radius: 16rpx;
					padding: 0 30rpx;
					font-size: 28rpx;
					color: #333;
				}
			}

			&.bio-item {
				.textarea-section {
					position: relative;

					.textarea {
						width: 100%;
						min-height: 200rpx;
						background: #f8f8f8;
						border-radius: 16rpx;
						padding: 20rpx 30rpx;
						font-size: 28rpx;
						color: #333;
						box-sizing: border-box;
					}

					.char-count {
						text-align: right;
						font-size: 24rpx;
						color: #999;
						margin-top: 10rpx;
					}
				}
			}
		}

		.save-btn {
			width: 100%;
			height: 100rpx;
			background: linear-gradient(135deg, #7a544d 0%, #613942 100%);
			color: #fff;
			border-radius: 50rpx;
			border: none;
			box-shadow: 0 16rpx 48rpx rgba(122, 84, 77, 0.4);
			font-size: 32rpx;
			font-weight: 600;
			margin-top: 40rpx;

			&::after {
				border: none;
			}
		}
	}
}
</style>
