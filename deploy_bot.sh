#!/bin/bash

# --- 配置部分 ---
IMAGE_NAME="unitserow/example-telegrambot"
IMAGE_TAG="latest"
CONTAINER_NAME="example-telegrambot-container"
HOST_PORT="8080" # 宿主机端口
CONTAINER_PORT="8080" # 容器内部端口 (请根据你的应用实际端口修改)
# --- 配置结束 ---

echo "--- 开始部署 Docker 镜像 ---"

# 1. 检查 Docker 服务是否运行
if ! systemctl is-active --quiet docker; then
    echo "错误：Docker 服务未运行。请启动 Docker 后重试。"
    echo "你可以尝试执行 'sudo systemctl start docker' 或 'sudo service docker start'"
    exit 1
fi
echo "Docker 服务正在运行。"

# 2. 停止并移除已存在的同名容器（如果存在）
echo "尝试停止并移除旧的容器 '${CONTAINER_NAME}' (如果存在)..."
if docker ps -a --format '{{.Names}}' | grep -q "${CONTAINER_NAME}"; then
    docker stop "${CONTAINER_NAME}"
    docker rm "${CONTAINER_NAME}"
    echo "旧容器 '${CONTAINER_NAME}' 已停止并移除。"
else
    echo "没有找到名为 '${CONTAINER_NAME}' 的旧容器。"
fi

# 3. 拉取最新的 Docker 镜像
FULL_IMAGE_NAME="${IMAGE_NAME}:${IMAGE_TAG}"
echo "正在拉取 Docker 镜像：${FULL_IMAGE_NAME}..."
if ! docker pull "${FULL_IMAGE_NAME}"; then
    echo "错误：无法拉取 Docker 镜像 '${FULL_IMAGE_NAME}'。请检查网络连接或镜像名称。"
    exit 1
fi
echo "镜像 '${FULL_IMAGE_NAME}' 拉取成功。"

# 4. 运行 Docker 容器
echo "正在运行 Docker 容器 '${CONTAINER_NAME}'..."
# -d: 后台运行容器
# -p: 端口映射 (宿主机端口:容器内部端口)
# --name: 指定容器名称
if ! docker run -d -p "${HOST_PORT}:${CONTAINER_PORT}" --name "${CONTAINER_NAME}" "${FULL_IMAGE_NAME}"; then
    echo "错误：无法运行 Docker 容器 '${CONTAINER_NAME}'。可能端口 '${HOST_PORT}' 已被占用或容器内部应用配置问题。"
    exit 1
fi
echo "容器 '${CONTAINER_NAME}' 已成功启动。"

# 5. 显示容器状态
echo "当前运行的 Docker 容器状态："
docker ps | grep "${CONTAINER_NAME}"

echo "--- 部署完成！---"
echo "你现在可以通过访问 http://localhost:${HOST_PORT} 来尝试访问你的应用 (如果它提供了Web界面)。"