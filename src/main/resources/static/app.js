let currentUser = null;
let currentChatUser = null;
let currentGroup = null;

const API = "/api";

let messageRefreshTimer = null;
let heartbeatTimer = null;
let statusRefreshTimer = null;


// ===============================
// AUTH
// ===============================

function showLogin() {
    document.getElementById("loginForm").classList.remove("hidden");
    document.getElementById("registerForm").classList.add("hidden");

    document.getElementById("loginTab").classList.add("active");
    document.getElementById("registerTab").classList.remove("active");
}

function showRegister() {
    document.getElementById("loginForm").classList.add("hidden");
    document.getElementById("registerForm").classList.remove("hidden");

    document.getElementById("loginTab").classList.remove("active");
    document.getElementById("registerTab").classList.add("active");
}


async function register() {

    const username =
        document.getElementById("registerUsername").value.trim();

    const email =
        document.getElementById("registerEmail").value.trim();

    const password =
        document.getElementById("registerPassword").value;

    const confirm =
        document.getElementById("registerConfirm").value;

    const message =
        document.getElementById("authMessage");

    if (!username || !email || !password || !confirm) {
        message.textContent = "Please fill all fields.";
        message.className = "error-message";
        return;
    }

    if (password.length < 6) {
        message.textContent =
            "Password must contain at least 6 characters.";
        message.className = "error-message";
        return;
    }

    if (password !== confirm) {
        message.textContent =
            "Passwords do not match.";
        message.className = "error-message";
        return;
    }

    try {

        const response = await fetch(
            API + "/auth/register",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username,
                    email,
                    password
                })
            }
        );

        const text = await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
            data = {};
        }

        if (!response.ok) {
            message.textContent =
                data.message || text || "Registration failed.";
            message.className = "error-message";
            return;
        }

        message.textContent =
            "Registration successful. You can now login.";

        message.className = "success-message";

        document.getElementById("loginUsername").value =
            username;

        document.getElementById("loginPassword").value =
            "";

        showLogin();

    } catch (error) {

        console.error(error);

        message.textContent =
            "Cannot connect to server.";

        message.className = "error-message";
    }
}


async function login() {

    const username =
        document.getElementById("loginUsername").value.trim();

    const password =
        document.getElementById("loginPassword").value;

    const message =
        document.getElementById("authMessage");

    if (!username || !password) {
        message.textContent =
            "Enter username and password.";
        message.className = "error-message";
        return;
    }

    try {

        const response = await fetch(
            API + "/auth/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    username,
                    password
                })
            }
        );

        const text = await response.text();

        let data;

        try {
            data = JSON.parse(text);
        } catch {
            data = {};
        }

        if (!response.ok) {

            message.textContent =
                data.message || text || "Login failed.";

            message.className = "error-message";

            return;
        }

        currentUser = data;

        localStorage.setItem(
            "chatUser",
            JSON.stringify(data)
        );

        openChatPage();

    } catch (error) {

        console.error(error);

        message.textContent =
            "Cannot connect to server.";

        message.className = "error-message";
    }
}


// ===============================
// OPEN CHAT
// ===============================

function openChatPage() {

    document
        .getElementById("authPage")
        .classList.add("hidden");

    document
        .getElementById("chatPage")
        .classList.remove("hidden");

    document.getElementById("currentUser")
        .textContent = currentUser.username;

    loadContacts();
    loadContactRequests();
    loadGroups();
    loadMeetings();

    startHeartbeat();
}
// ===============================
// USERS
// ===============================

async function loadContacts() {

    try {

        const response =
            await fetch(
                API +
                "/contacts/" +
                currentUser.id
            );

        if (!response.ok) {

            console.error(
                "Unable to load contacts"
            );

            return;
        }

        const contacts =
            await response.json();

        const container =
            document.getElementById(
                "contactsList"
            );

        container.innerHTML = "";

        if (!contacts.length) {

            container.innerHTML = `
                <div class="empty-list">
                    No contacts yet.
                    <br>
                    Search for someone to add.
                </div>
            `;

            return;
        }

        contacts.forEach(user => {

            const div =
                document.createElement("div");

            div.className =
                "user-item";

            div.dataset.userId =
                user.id;

            const status =
                user.status || "OFFLINE";

            div.innerHTML = `
                <div class="user-name">
                    @${escapeHtml(user.username)}
                </div>

                <div class="user-status ${
                    status === "ONLINE"
                        ? "online"
                        : "offline"
                }">

                    <span class="status-dot"></span>

                    ${escapeHtml(status)}

                </div>
            `;

            div.onclick = () =>
                openUserChat(user);

            container.appendChild(div);
        });

    } catch (error) {

        console.error(
            "Contact loading error:",
            error
        );
    }
}




async function refreshContactStatuses() {

    if (!currentUser) {
        return;
    }

    try {

        const response = await fetch(
            API + "/contacts/" + currentUser.id
        );

        if (!response.ok) {
            return;
        }

        const contacts = await response.json();

        contacts.forEach(user => {

            const item = document.querySelector(
                `.user-item[data-user-id="${user.id}"]`
            );

            if (!item) {
                return;
            }

            const statusElement =
                item.querySelector(".user-status");

            const dot =
                item.querySelector(".status-dot");

            const status =
                user.status || "OFFLINE";

            if (statusElement) {

                statusElement.textContent = status;

                if (dot) {
                    statusElement.prepend(dot);
                }

                statusElement.classList.toggle(
                    "online",
                    status === "ONLINE"
                );

                statusElement.classList.toggle(
                    "offline",
                    status !== "ONLINE"
                );
            }
        });

        if (currentChatUser) {

            const response =
                await fetch(
                    API +
                    "/auth/status/" +
                    currentChatUser.id
                );

            if (response.ok) {

                const data =
                    await response.json();

                currentChatUser.status =
                    data.status;

                const chatStatus =
                    document.getElementById(
                        "chatStatus"
                    );

                if (chatStatus) {
                    chatStatus.textContent =
                        data.status;
                }
            }
        }

    } catch (error) {

        console.error(
            "Status refresh error:",
            error
        );
    }
}
function startStatusRefresh() {
    // Automatic status refresh disabled.
}

// ===============================
// PRIVATE CHAT
// ===============================

function openUserChat(user) {

    currentChatUser = user;
    currentGroup = null;

    document.getElementById("chatTitle")
        .textContent = user.username;

    document.getElementById("chatStatus")
        .textContent =
            user.status || "OFFLINE";

    document.getElementById("messages")
        .innerHTML = "";

    loadMessages();
}


async function loadMessages() {

    if (!currentChatUser) {
        return;
    }

    try {

        const response = await fetch(
            API +
            "/messages/private/" +
            currentUser.id +
            "/" +
            currentChatUser.id
        );

        if (!response.ok) {

            const errorText =
                await response.text();

            console.error(
                "Message API error:",
                errorText
            );

            return;
        }

        const messages =
            await response.json();

        const container =
            document.getElementById("messages");

        const wasAtBottom =
            container.scrollHeight -
            container.scrollTop -
            container.clientHeight < 100;

        container.innerHTML = "";

        if (!messages.length) {

            container.innerHTML = `
                <div class="empty-chat">
                    <div class="empty-icon">💬</div>
                    <h3>No messages yet</h3>
                    <p>Send a message to start the conversation.</p>
                </div>
            `;

            return;
        }

        messages.forEach(renderMessage);

        if (wasAtBottom) {
            container.scrollTop =
                container.scrollHeight;
        }

    } catch (error) {

        console.error(
            "Loading messages failed:",
            error
        );
    }
}


// ===============================
// RENDER MESSAGE
// ===============================

function renderMessage(message) {

    const container = document.getElementById("messages");

    const div = document.createElement("div");

    const senderId =
        message.senderId ??
        message.sender?.id;

    const mine =
        Number(senderId) === Number(currentUser.id);

    div.className =
        "message-row " + (mine ? "mine" : "theirs");

    let content =
        message.content ||
        message.message ||
        "";

    let attachmentHtml = "";

    if (message.fileUrl) {

        const fileName =
            message.fileName || "Attached file";

        const fileType =
            message.fileType || "";

        const fileUrl =
            message.fileUrl;

        const isImage =
            fileType.startsWith("image/") ||
            /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(fileName);

        if (isImage) {

            attachmentHtml = `
                <div class="image-message">
                    <a
                        href="${escapeHtml(fileUrl)}"
                        target="_blank"
                        rel="noopener noreferrer">

                        <img
                            src="${escapeHtml(fileUrl)}"
                            alt="${escapeHtml(fileName)}"
                            loading="lazy">
                    </a>

                    <div class="attachment-name">
                        ${escapeHtml(fileName)}
                    </div>
                </div>
            `;

        } else {

            attachmentHtml = `
                <div class="file-message">

                    <div class="file-icon">
                        📄
                    </div>

                    <div class="file-info">

                        <a
                            href="${escapeHtml(fileUrl)}"
                            target="_blank"
                            rel="noopener noreferrer">

                            ${escapeHtml(fileName)}

                        </a>

                        <small>
                            ${escapeHtml(fileType || "File")}
                        </small>

                    </div>

                    <a
                        class="download-file"
                        href="${escapeHtml(fileUrl)}"
                        download="${escapeHtml(fileName)}">

                        ⬇️

                    </a>

                </div>
            `;
        }
    }

    div.innerHTML = `
        <div class="message-bubble">

            ${
                content
                    ? `
                        <div class="message-content">
                            ${escapeHtml(content)}
                        </div>
                    `
                    : ""
            }

            ${attachmentHtml}

            <div class="message-time">
                ${formatTime(message.createdAt)}
            </div>

        </div>
    `;

    container.appendChild(div);

    container.scrollTop = container.scrollHeight;
}

// ===============================
// SEND MESSAGE
// ===============================

async function sendMessage() {

    const input =
        document.getElementById("messageInput");

    const sendButton =
        document.querySelector(".send-btn");

    const content =
        input.value.trim();

    if (!content) {
        return;
    }

    if (!currentChatUser && !currentGroup) {

        showToast(
            "Select a contact or group first.",
            "error"
        );

        return;
    }

    const body = {
        senderId: currentUser.id,
        content: content
    };

    if (currentChatUser) {

        body.receiverId =
            currentChatUser.id;
    }

    if (currentGroup) {

        body.groupId =
            currentGroup.id;
    }

    sendButton.disabled = true;

    try {

        const response = await fetch(
            API + "/messages",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/json"
                },

                body: JSON.stringify(body)
            }
        );

        const text =
            await response.text();

        if (!response.ok) {

            console.error(
                "SEND MESSAGE ERROR:",
                response.status,
                text
            );

            showToast(
                "Message failed: " +
                (text || response.status),
                "error"
            );

            return;
        }

        input.value = "";

        await loadMessages();

    } catch (error) {

        console.error(
            "Send message error:",
            error
        );

        showToast(
            "Could not send message.",
            "error"
        );

    } finally {

        sendButton.disabled = false;

        input.focus();
    }
}


function handleEnter(event) {

    if (
        event.key === "Enter" &&
        !event.shiftKey
    ) {

        event.preventDefault();

        sendMessage();
    }
}


// ===============================
// REAL-TIME REFRESH
// ===============================


function startMessageRefresh() {
    // Automatic message refresh disabled.
}


// ===============================
// FILE UPLOAD
// ===============================

async function uploadFile() {

    const input =
        document.getElementById("fileInput");

    const file =
        input.files[0];

    if (!file) {
        return;
    }

    if (!currentChatUser && !currentGroup) {

        showToast(
            "Select a contact or group before uploading.",
            "error"
        );

        input.value = "";

        return;
    }

    const MAX_SIZE =
        100 * 1024 * 1024;

    if (file.size > MAX_SIZE) {

        showToast(
            "Maximum file size is 100 MB.",
            "error"
        );

        input.value = "";

        return;
    }

    showToast(
        "Uploading " + file.name + "...",
        "info"
    );

    const formData = new FormData();

    formData.append("file", file);

    formData.append(
        "senderId",
        currentUser.id
    );

    if (currentChatUser) {

        formData.append(
            "receiverId",
            currentChatUser.id
        );
    }

    if (currentGroup) {

        formData.append(
            "groupId",
            currentGroup.id
        );
    }

    try {

        // =========================
        // STEP 1: UPLOAD FILE
        // =========================

        const response =
            await fetch(
                API + "/files/upload",
                {
                    method: "POST",
                    body: formData
                }
            );

        const text =
            await response.text();

        if (!response.ok) {

            console.error(
                "FILE UPLOAD ERROR:",
                response.status,
                text
            );

            showToast(
                "File upload failed.",
                "error"
            );

            return;
        }

        console.log(
            "Upload response:",
            text
        );

        let uploadedFile;

        try {

            uploadedFile =
                JSON.parse(text);

        } catch (error) {

            console.error(
                "Invalid upload response:",
                text
            );

            showToast(
                "Invalid server response.",
                "error"
            );

            return;
        }


        // =========================
        // STEP 2: CREATE MESSAGE
        // =========================

        const messageBody = {

            senderId:
                currentUser.id,

            content: "",

            fileName:
                uploadedFile.originalName,

            fileType:
                uploadedFile.contentType,

            fileUrl:
                uploadedFile.fileUrl
        };


        if (currentChatUser) {

            messageBody.receiverId =
                currentChatUser.id;
        }


        if (currentGroup) {

            messageBody.groupId =
                currentGroup.id;
        }


        const messageResponse =
            await fetch(
                API + "/messages",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(messageBody)
                }
            );


        const messageText =
            await messageResponse.text();


        if (!messageResponse.ok) {

            console.error(
                "FILE MESSAGE ERROR:",
                messageResponse.status,
                messageText
            );

            showToast(
                "File uploaded, but message creation failed.",
                "error"
            );

            return;
        }


        // =========================
        // STEP 3: REFRESH CHAT
        // =========================

        input.value = "";

        showToast(
            "File sent successfully.",
            "success"
        );


        if (currentChatUser) {

            await loadMessages();
        }


        if (currentGroup) {

            await loadGroupMessages();
        }


    } catch (error) {

        console.error(
            "Upload error:",
            error
        );

        showToast(
            "File upload failed.",
            "error"
        );

    } finally {

        input.value = "";
    }
}


// ===============================
// GROUPS
// ===============================

function showCreateGroup() {

    document
        .getElementById("groupModal")
        .classList.remove("hidden");
}


async function createGroup() {

    const name =
        document.getElementById("groupName")
            .value.trim();

    if (!name) {

        showToast(
            "Enter group name.",
            "error"
        );

        return;
    }

    if (!currentUser || !currentUser.id) {

        showToast(
            "Please login again.",
            "error"
        );

        return;
    }

    try {

        /*
         * Backend expects:
         *
         * @RequestParam String name
         * @RequestParam Long creatorId
         *
         * Therefore we send URL parameters,
         * not JSON.
         */

        const params = new URLSearchParams();

        params.append("name", name);
        params.append(
            "creatorId",
            currentUser.id
        );

        const response =
            await fetch(
                API + "/groups?" + params.toString(),
                {
                    method: "POST"
                }
            );

        const text =
            await response.text();

        console.log(
            "CREATE GROUP RESPONSE:",
            response.status,
            text
        );

        if (!response.ok) {

            showToast(
                "Unable to create group: " +
                text,
                "error"
            );

            return;
        }

        closeModals();

        document.getElementById("groupName")
            .value = "";

        await loadGroups();

        showToast(
            "Group created successfully.",
            "success"
        );

    } catch (error) {

        console.error(
            "GROUP CREATION ERROR:",
            error
        );

        showToast(
            "Group creation failed.",
            "error"
        );
    }
}


async function loadGroups() {

    try {

        const response =
            await fetch(
                API +
                "/groups/user/" +
                currentUser.id
            );

        if (!response.ok) {
            console.error(
                "Unable to load groups"
            );
            return;
        }

        const groups =
            await response.json();

        const container =
            document.getElementById(
                "groupsList"
            );

        container.innerHTML = "";

        if (!groups.length) {

            container.innerHTML = `
                <div class="empty-list">
                    No groups yet.
                    <br>
                    Create a group to get started.
                </div>
            `;

            return;
        }

        groups.forEach(group => {

            const div =
                document.createElement("div");

            div.className =
                "group-item";

            div.innerHTML = `
                <div class="group-icon">
                    👥
                </div>

                <div class="group-info">
                    <strong>
                        ${escapeHtml(group.name)}
                    </strong>

                    <small>
                        ${
                            Number(group.creatorId) ===
                            Number(currentUser.id)
                                ? "Created by you"
                                : "Group"
                        }
                    </small>
                </div>
            `;

            div.onclick = () =>
                openGroupChat(group);

            container.appendChild(div);
        });

    } catch (error) {

        console.error(
            "Group loading error:",
            error
        );
    }
}

function openGroupChat(group) {

    currentGroup = group;
    currentChatUser = null;

    document.getElementById("chatTitle")
        .textContent =
            "👥 " + group.name;

    document.getElementById("chatStatus")
        .textContent =
            Number(group.creatorId) ===
            Number(currentUser.id)
                ? "You are the group creator"
                : "Group chat";

    document.getElementById("messages")
        .innerHTML = "";

    createGroupHeaderActions();

    loadGroupMessages();
}

function createGroupHeaderActions() {

    const header =
        document.querySelector(".chat-header");

    if (!header || !currentGroup) {
        return;
    }

    const oldActions =
        document.getElementById("groupActions");

    if (oldActions) {
        oldActions.remove();
    }

    const actions =
        document.createElement("div");

    actions.id = "groupActions";
    actions.className = "group-actions";

    // =========================
    // GROUP INFO
    // =========================

    const infoButton =
        document.createElement("button");

    infoButton.className =
        "group-action-btn group-info-btn";

    infoButton.innerHTML =
        "ℹ Group Info";

    infoButton.onclick =
        function () {
            openGroupInfo();
        };

    actions.appendChild(infoButton);


    // =========================
    // CREATOR ONLY
    // =========================

    if (
        Number(currentGroup.creatorId) ===
        Number(currentUser.id)
    ) {

        const addButton =
            document.createElement("button");

        addButton.className =
            "group-action-btn add-member-btn";

        addButton.innerHTML =
            "＋ Add Member";

        addButton.onclick =
            function () {
                openAddMemberModal();
            };

        actions.appendChild(addButton);


        const deleteButton =
            document.createElement("button");

        deleteButton.className =
            "group-action-btn delete-group-btn";

        deleteButton.innerHTML =
            "🗑 Delete Group";

        deleteButton.onclick =
            function () {
                deleteCurrentGroup();
            };

        actions.appendChild(deleteButton);

    } else {

        // =========================
        // NORMAL MEMBER
        // =========================

        const leaveButton =
            document.createElement("button");

        leaveButton.className =
            "group-action-btn leave-group-btn";

        leaveButton.innerHTML =
            "Leave Group";

        leaveButton.onclick =
            function () {
                leaveCurrentGroup();
            };

        actions.appendChild(leaveButton);
    }

    header.appendChild(actions);
}

// ===============================
// GROUP INFO
// ===============================

async function openGroupInfo() {

    if (!currentGroup) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/groups/" +
                currentGroup.id +
                "/info"
            );

        if (!response.ok) {

            showToast(
                "Unable to load group information.",
                "error"
            );

            return;
        }

        const group =
            await response.json();

        renderGroupInfo(group);

        document
            .getElementById("groupInfoModal")
            .classList.remove("hidden");

    } catch (error) {

        console.error(
            "Group info error:",
            error
        );

        showToast(
            "Unable to load group information.",
            "error"
        );
    }
}


async function removeGroupMember(userId, username) {

    if (!currentGroup) {
        return;
    }

    if (
        Number(currentGroup.creatorId) !==
        Number(currentUser.id)
    ) {

        showToast(
            "Only the group creator can remove members.",
            "error"
        );

        return;
    }

    if (
        Number(userId) ===
        Number(currentGroup.creatorId)
    ) {

        showToast(
            "The group creator cannot be removed.",
            "error"
        );

        return;
    }

    const confirmed =
        confirm(
            'Remove @' +
            username +
            ' from this group?'
        );

    if (!confirmed) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/groups/" +
                currentGroup.id +
                "/members/" +
                userId +
                "?requesterId=" +
                currentUser.id,
                {
                    method: "DELETE"
                }
            );

        const text =
            await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                "Unable to remove member.",
                "error"
            );

            return;
        }

        showToast(
            "Member removed.",
            "success"
        );

        // Reload group information
        await openGroupInfo();

        // Refresh group list
        await loadGroups();

    } catch (error) {

        console.error(
            "Remove member error:",
            error
        );

        showToast(
            "Unable to remove member.",
            "error"
        );
    }
}

function renderGroupInfo(group) {

    const container =
        document.getElementById(
            "groupInfoContent"
        );

    if (!container) {
        return;
    }

    const members =
        group.members || [];

    const isCreator =
        Number(group.creatorId) ===
        Number(currentUser.id);

    container.innerHTML = `

        <div class="group-info-header">

            <div class="large-group-icon">
                👥
            </div>

            <h2>
                ${escapeHtml(group.name)}
            </h2>

            <p>
                ${members.length}
                ${members.length === 1 ? "member" : "members"}
            </p>

        </div>

        <div class="members-title">
            Members
        </div>

        <div class="group-members-list">

            ${
                members.length
                    ? members.map(member => {

                        const isAdmin =
                            member.admin === true;

                        const isOnline =
                            member.status === "ONLINE";

                        return `

                            <div class="group-member">

                                <div class="member-avatar">
                                    ${escapeHtml(
                                        member.username
                                            .charAt(0)
                                            .toUpperCase()
                                    )}
                                </div>

                                <div class="member-details">

                                    <strong>
                                        @${escapeHtml(
                                            member.username
                                        )}
                                    </strong>

                                    <span class="
                                        member-status
                                        ${
                                            isOnline
                                                ? "member-online"
                                                : "member-offline"
                                        }
                                    ">

                                        <span class="member-status-dot"></span>

                                        ${
                                            isOnline
                                                ? "Online"
                                                : "Offline"
                                        }

                                    </span>

                                </div>

                                ${
                                    isAdmin
                                        ? `
                                            <span class="admin-badge">
                                                👑 Admin
                                            </span>
                                          `
                                        : ""
                                }

                                ${
                                    isCreator &&
                                    !isAdmin
                                        ? `
                                            <button
                                                class="remove-member-btn"
                                                onclick="
                                                    removeGroupMember(
                                                        ${member.id},
                                                        '${escapeHtml(
                                                            member.username
                                                        )}'
                                                    )
                                                "
                                            >
                                                Remove
                                            </button>
                                          `
                                        : ""
                                }

                            </div>
                        `;

                    }).join("")
                    : `
                        <div class="empty-list">
                            No members.
                        </div>
                    `
            }

        </div>
    `;
}


function openAddMemberModal() {

    if (!currentGroup) {
        return;
    }

    document
        .getElementById("addMemberModal")
        .classList.remove("hidden");

    document.getElementById(
        "memberSearchInput"
    ).value = "";

    document.getElementById(
        "memberSearchResults"
    ).innerHTML = `
        <div class="empty-list">
            Search for a username to add.
        </div>
    `;

    document.getElementById(
        "memberSearchInput"
    ).focus();
}


function closeAddMemberModal() {

    document
        .getElementById("addMemberModal")
        .classList.add("hidden");
}


async function searchGroupMembers() {

    const query =
        document.getElementById(
            "memberSearchInput"
        ).value.trim();

    const container =
        document.getElementById(
            "memberSearchResults"
        );

    if (!query) {

        container.innerHTML = `
            <div class="empty-list">
                Enter a username.
            </div>
        `;

        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/search/users?q=" +
                encodeURIComponent(query)
            );

        if (!response.ok) {

            container.innerHTML = `
                <div class="empty-list">
                    Search failed.
                </div>
            `;

            return;
        }

        const users =
            await response.json();

        container.innerHTML = "";

        const filtered =
            users.filter(
                user =>
                    Number(user.id) !==
                    Number(currentUser.id)
            );

        if (!filtered.length) {

            container.innerHTML = `
                <div class="empty-list">
                    No user found.
                </div>
            `;

            return;
        }

        filtered.forEach(user => {

            const row =
                document.createElement("div");

            row.className =
                "member-search-result";

            row.innerHTML = `
                <div class="member-user-info">

                    <strong>
                        @${escapeHtml(user.username)}
                    </strong>

                    <small class="${
                        user.status === "ONLINE"
                            ? "online-text"
                            : ""
                    }">
                        ${escapeHtml(
                            user.status || "OFFLINE"
                        )}
                    </small>

                </div>

                <button
                    class="member-add-button"
                    onclick="addMemberToGroup(
                        ${user.id}
                    )">

                    Add

                </button>
            `;

            container.appendChild(row);
        });

    } catch (error) {

        console.error(
            "Group member search error:",
            error
        );
    }
}


async function addMemberToGroup(userId) {

    if (!currentGroup) {
        return;
    }

    if (
        Number(currentGroup.creatorId) !==
        Number(currentUser.id)
    ) {

        showToast(
            "Only the group creator can add members.",
            "error"
        );

        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/groups/" +
                currentGroup.id +
                "/members/" +
                userId +
                "?requesterId=" +
                currentUser.id,
                {
                    method: "POST"
                }
            );

        const text =
            await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                "Unable to add member.",
                "error"
            );

            return;
        }

        showToast(
            "Member added successfully.",
            "success"
        );

        closeAddMemberModal();

        await loadGroups();

        // Refresh current group from backend
        const groupsResponse =
            await fetch(
                API +
                "/groups/user/" +
                currentUser.id
            );

        if (groupsResponse.ok) {

            const groups =
                await groupsResponse.json();

            const updated =
                groups.find(
                    g =>
                        Number(g.id) ===
                        Number(currentGroup.id)
                );

            if (updated) {
                currentGroup = updated;
            }
        }

    } catch (error) {

        console.error(
            "Add member error:",
            error
        );

        showToast(
            "Unable to add member.",
            "error"
        );
    }
}


async function deleteCurrentGroup() {

    if (!currentGroup) {
        return;
    }

    if (
        Number(currentGroup.creatorId) !==
        Number(currentUser.id)
    ) {

        showToast(
            "Only the group creator can delete the group.",
            "error"
        );

        return;
    }

    const confirmed =
        confirm(
            'Are you sure you want to permanently delete "' +
            currentGroup.name +
            '"?'
        );

    if (!confirmed) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/groups/" +
                currentGroup.id +
                "?requesterId=" +
                currentUser.id,
                {
                    method: "DELETE"
                }
            );

        const text =
            await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                "Unable to delete group.",
                "error"
            );

            return;
        }

        showToast(
            "Group deleted successfully.",
            "success"
        );

        currentGroup = null;

        const actions =
            document.getElementById(
                "groupActions"
            );

        if (actions) {
            actions.remove();
        }

        document.getElementById(
            "chatTitle"
        ).textContent =
            "Select a conversation";

        document.getElementById(
            "chatStatus"
        ).textContent = "";

        document.getElementById(
            "messages"
        ).innerHTML = `
            <div class="welcome">

                <h1>
                    Welcome to ChatApp 👋
                </h1>

                <p>
                    Select a contact or group to start chatting.
                </p>

            </div>
        `;

        await loadGroups();

    } catch (error) {

        console.error(
            "Delete group error:",
            error
        );

        showToast(
            "Unable to delete group.",
            "error"
        );
    }
}


async function leaveCurrentGroup() {

    if (!currentGroup) {
        return;
    }

    const confirmed =
        confirm(
            'Leave "' +
            currentGroup.name +
            '"?'
        );

    if (!confirmed) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/groups/" +
                currentGroup.id +
                "/leave/" +
                currentUser.id,
                {
                    method: "DELETE"
                }
            );

        const text =
            await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                "Unable to leave group.",
                "error"
            );

            return;
        }

        showToast(
            "You left the group.",
            "success"
        );

        currentGroup = null;

        const actions =
            document.getElementById(
                "groupActions"
            );

        if (actions) {
            actions.remove();
        }

        document.getElementById(
            "chatTitle"
        ).textContent =
            "Select a conversation";

        document.getElementById(
            "chatStatus"
        ).textContent = "";

        document.getElementById(
            "messages"
        ).innerHTML = `
            <div class="welcome">

                <h1>
                    Welcome to ChatApp 👋
                </h1>

                <p>
                    Select a contact or group to start chatting.
                </p>

            </div>
        `;

        await loadGroups();

    } catch (error) {

        console.error(
            "Leave group error:",
            error
        );

        showToast(
            "Unable to leave group.",
            "error"
        );
    }
}

async function loadGroupMessages() {

    if (!currentGroup) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/messages/group/" +
                currentGroup.id
            );

        if (!response.ok) {
            return;
        }

        const messages =
            await response.json();

        const container =
            document.getElementById("messages");

        container.innerHTML = "";

        if (!messages.length) {

            container.innerHTML = `
                <div class="empty-chat">
                    <div class="empty-icon">👥</div>
                    <h3>No messages yet</h3>
                    <p>Start the group conversation.</p>
                </div>
            `;

            return;
        }

        messages.forEach(renderMessage);

    } catch (error) {

        console.error(error);
    }
}


// ===============================
// SEARCH
// ===============================

async function searchUsers() {

    const query =
        document.getElementById("searchInput")
            .value.trim();

    const container =
        document.getElementById(
            "contactsList"
        );


    // Empty search → show contacts
    if (!query) {

        loadContacts();

        return;
    }


    try {

        const response =
            await fetch(
                API +
                "/search/users?q=" +
                encodeURIComponent(query)
            );

        if (!response.ok) {

            console.error(
                "Search failed"
            );

            return;
        }

        const users =
            await response.json();

        container.innerHTML = "";


        const filteredUsers =
            users.filter(
                user =>
                    Number(user.id) !==
                    Number(currentUser.id)
            );


        if (!filteredUsers.length) {

            container.innerHTML = `
                <div class="empty-list">
                    No user found.
                </div>
            `;

            return;
        }


        filteredUsers.forEach(user => {

            const div =
                document.createElement("div");

            div.className =
                "search-result";

            div.innerHTML = `

                <div class="search-user-info">

                    <strong>
                        @${escapeHtml(
                            user.username
                        )}
                    </strong>

                    <small>
                        ${
                            escapeHtml(
                                user.status ||
                                "OFFLINE"
                            )
                        }
                    </small>

                </div>

                <button
                    class="add-contact-btn"
                    onclick="
                        sendContactRequest(
                            ${user.id},
                            event
                        )
                    "
                >
                    + Add
                </button>

            `;

            container.appendChild(div);
        });

    } catch (error) {

        console.error(
            "Search error:",
            error
        );
    }
}




async function sendContactRequest(
    contactUserId,
    event
) {

    if (event) {
        event.stopPropagation();
    }

    try {

        const response =
            await fetch(
                API +
                "/contacts/request",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        userId:
                            currentUser.id,

                        contactUserId:
                            contactUserId
                    })
                }
            );

        const text =
            await response.text();

        let data = {};

        try {
            data = JSON.parse(text);
        } catch {
        }


        if (!response.ok) {

            showToast(
                data.message ||
                "Unable to send request.",
                "error"
            );

            return;
        }


        showToast(
            data.message ||
            "Contact request sent.",
            "success"
        );

    } catch (error) {

        console.error(
            "Contact request error:",
            error
        );

        showToast(
            "Could not send contact request.",
            "error"
        );
    }
}


async function loadContactRequests() {

    try {

        const response =
            await fetch(
                API +
                "/contacts/requests/" +
                currentUser.id
            );

        if (!response.ok) {
            return;
        }

        const requests =
            await response.json();

        updateNotificationDot(requests.length);

        const container =
            document.getElementById(
                "requestsList"
            );

        if (!container) {
            return;
        }

        container.innerHTML = "";


        if (!requests.length) {

            container.innerHTML = `
                <div class="empty-list">
                    No pending requests.
                </div>
            `;

            return;
        }


        requests.forEach(request => {

            const div =
                document.createElement("div");

            div.className =
                "contact-request";

            div.innerHTML = `

                <div class="request-user">

                    @${escapeHtml(
                        request.username
                    )}

                </div>

                <div class="request-actions">

                    <button
                        onclick="
                            acceptContact(
                                ${request.id}
                            )
                        "
                    >
                        ✓ Accept
                    </button>

                    <button
                        onclick="
                            rejectContact(
                                ${request.id}
                            )
                        "
                    >
                        ✕ Reject
                    </button>

                </div>
            `;

            container.appendChild(div);
        });

    } catch (error) {

        console.error(
            "Request loading error:",
            error
        );
    }
}



async function acceptContact(requestId) {

    try {

        const response =
            await fetch(
                API +
                "/contacts/" +
                requestId +
                "/accept",
                {
                    method: "POST"
                }
            );

        if (!response.ok) {

            showToast(
                "Unable to accept request.",
                "error"
            );

            return;
        }

        showToast(
            "Contact added successfully.",
            "success"
        );

        await loadContactRequests();
        await loadContacts();

    } catch (error) {

        console.error(error);

        showToast(
            "Something went wrong.",
            "error"
        );
    }
}


async function rejectContact(requestId) {

    try {

        const response =
            await fetch(
                API +
                "/contacts/" +
                requestId +
                "/reject",
                {
                    method: "POST"
                }
            );

        if (!response.ok) {

            showToast(
                "Unable to reject request.",
                "error"
            );

            return;
        }

        showToast(
            "Request rejected.",
            "info"
        );

        await loadContactRequests();

    } catch (error) {

        console.error(error);
    }
}

let allMeetings = [];




async function loadMeetings() {

    if (!currentUser || !currentUser.id) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/meetings/user/" +
                currentUser.id
            );

        if (!response.ok) {

            console.error(
                "Unable to load meetings:",
                response.status
            );

            return;
        }

        const meetings =
            await response.json();

        allMeetings =
            Array.isArray(meetings)
                ? meetings
                : [];

        renderMeetings(
            allMeetings
        );

    } catch (error) {

        console.error(
            "Meeting loading error:",
            error
        );
    }
}

function filterMeetings() {

    const input =
        document.getElementById(
            "meetingSearchInput"
        );

    if (!input) {
        return;
    }

    const query =
        input.value
            .trim()
            .toLowerCase();

    if (!query) {

        renderMeetings(
            allMeetings
        );

        return;
    }

    const filtered =
        allMeetings.filter(
            meeting => {

                const title =
                    String(
                        meeting.title || ""
                    ).toLowerCase();

                const code =
                    String(
                        meeting.meetingCode || ""
                    ).toLowerCase();

                const description =
                    String(
                        meeting.description || ""
                    ).toLowerCase();

                return (
                    title.includes(query) ||
                    code.includes(query) ||
                    description.includes(query)
                );

            }
        );

    renderMeetings(
        filtered
    );
}

// ===============================
// MEETING
// ===============================

function showMeeting() {

    const modal =
        document.getElementById("meetingModal");

    if (!modal) {
        return;
    }

    modal.classList.remove("hidden");

    const groupInfo =
        document.getElementById("meetingGroupInfo");

    if (currentGroup) {

        groupInfo.textContent =
            "Meeting for group: " +
            currentGroup.name;

    } else {

        groupInfo.textContent =
            "Personal Meeting";
    }

    const dateInput =
        document.getElementById("meetingDate");

    if (dateInput) {

        const now = new Date();

        now.setMinutes(
            now.getMinutes() -
            now.getTimezoneOffset()
        );

        dateInput.min =
            now.toISOString().slice(0, 16);
    }
}


// ===============================
// CREATE MEETING
// ===============================

async function createMeeting() {

    const title =
        document.getElementById("meetingTitle")
            .value.trim();

    const date =
        document.getElementById("meetingDate")
            .value;

    const description =
        document.getElementById("meetingDescription")
            .value.trim();

    if (!title) {

        showToast(
            "Enter a meeting title.",
            "error"
        );

        return;
    }

    if (!date) {

        showToast(
            "Select meeting date and time.",
            "error"
        );

        return;
    }

    if (!currentUser || !currentUser.id) {

        showToast(
            "Please login again.",
            "error"
        );

        return;
    }

    try {

        const body = {
            title: title,
            dateTime: date,
            description: description,
            createdBy: Number(currentUser.id),
            groupId: currentGroup
                ? Number(currentGroup.id)
                : null,
            durationMinutes: 60
        };

        console.log(
            "Creating meeting:",
            body
        );

        const response =
            await fetch(
                API + "/meetings",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(body)
                }
            );

        const text =
            await response.text();

        let data = {};

        try {

            data =
                JSON.parse(text);

        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                text ||
                "Meeting creation failed.",
                "error"
            );

            return;
        }

        // Clear form

        document.getElementById(
            "meetingTitle"
        ).value = "";

        document.getElementById(
            "meetingDate"
        ).value = "";

        document.getElementById(
            "meetingDescription"
        ).value = "";

        closeModals();

        await loadMeetings();

        showToast(
            "Meeting scheduled successfully.",
            "success"
        );

        // Show newly created meeting
        if (data && data.id) {

            setTimeout(() => {

                showMeetingDetails(data);

            }, 300);
        }

    } catch (error) {

        console.error(
            "Meeting creation error:",
            error
        );

        showToast(
            "Meeting creation failed.",
            "error"
        );
    }
}


// ===============================
// LOAD MEETINGS
// ===============================

async function loadMeetings() {

    if (!currentUser || !currentUser.id) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/meetings/user/" +
                currentUser.id
            );

        if (!response.ok) {

            console.error(
                "Unable to load meetings:",
                response.status
            );

            return;
        }

        const meetings =
            await response.json();

        renderMeetings(meetings);

    } catch (error) {

        console.error(
            "Meeting loading error:",
            error
        );
    }
}


// ===============================
// RENDER MEETINGS
// ===============================

function renderMeetings(meetings) {

    const container =
        document.getElementById(
            "meetingsList"
        );

    if (!container) {
        return;
    }

    container.innerHTML = "";

    if (!meetings || !meetings.length) {

        container.innerHTML = `
            <div class="empty-meetings">
                No meetings scheduled.
            </div>
        `;

        return;
    }

    meetings.forEach(meeting => {

        const item =
            document.createElement("div");

        item.className =
            "meeting-item";

        const status =
            meeting.status ||
            "UPCOMING";

        const statusClass =
            status.toLowerCase();

        item.innerHTML = `

            <div class="meeting-icon">
                📅
            </div>

            <div class="meeting-content">

                <div class="meeting-title">
                    ${escapeHtml(
                        meeting.title
                    )}
                </div>

                <div class="meeting-time">
                    ${escapeHtml(
                        formatMeetingDate(
                            meeting.dateTime
                        )
                    )}
                </div>

                <div class="meeting-code-preview">
                    Code:
                    ${escapeHtml(
                        meeting.meetingCode || "N/A"
                    )}
                </div>

                <span class="
                    meeting-status
                    ${statusClass}
                ">
                    ${escapeHtml(status)}
                </span>

            </div>

        `;

        item.onclick = function () {

            showMeetingDetails(
                meeting
            );

        };

        container.appendChild(item);
    });
}


// ===============================
// FORMAT MEETING DATE
// ===============================

function formatMeetingDate(value) {

    if (!value) {
        return "";
    }

    try {

        return new Date(value)
            .toLocaleString([], {

                day: "2-digit",

                month: "short",

                year: "numeric",

                hour: "2-digit",

                minute: "2-digit"

            });

    } catch {

        return String(value);
    }
}


// ===============================
// SHOW MEETING DETAILS
// ===============================

let selectedMeeting = null;


// ===============================
// SHOW MEETING DETAILS
// ===============================

function showMeetingDetails(meeting) {

    if (!meeting) {
        return;
    }

    selectedMeeting =
        meeting;

    document.getElementById(
        "detailsMeetingTitle"
    ).textContent =
        meeting.title || "Meeting";

    document.getElementById(
        "detailsMeetingDate"
    ).textContent =
        formatMeetingDate(
            meeting.dateTime
        );

    document.getElementById(
        "detailsMeetingCode"
    ).textContent =
        meeting.meetingCode ||
        "Not available";

    document.getElementById(
        "detailsMeetingType"
    ).textContent =
        meeting.groupId
            ? "Group Meeting"
            : "Personal Meeting";

    document.getElementById(
        "detailsMeetingDescription"
    ).textContent =
        meeting.description ||
        "No description";


    const status =
        meeting.status ||
        "UPCOMING";

    const statusElement =
        document.getElementById(
            "detailsMeetingStatus"
        );

    statusElement.textContent =
        status;

    statusElement.className =
        "meeting-status " +
        status.toLowerCase();


    const joinButton =
        document.getElementById(
            "joinMeetingDetailsBtn"
        );

    const deleteButton =
        document.getElementById(
            "deleteMeetingDetailsBtn"
        );


    // =========================
    // JOIN BUTTON
    // =========================

    if (status === "COMPLETED") {

        joinButton.disabled = true;

        joinButton.textContent =
            "Meeting Ended";

    } else {

        joinButton.disabled = false;

        joinButton.textContent =
            "▶ Join Meeting";

        joinButton.onclick =
            function () {

                openMeeting(
                    selectedMeeting
                );

            };
    }


    // =========================
    // DELETE BUTTON
    // =========================

    const isCreator =
        Number(meeting.createdBy) ===
        Number(currentUser.id);

    if (isCreator) {

        deleteButton.classList.remove(
            "hidden"
        );

        deleteButton.onclick =
            async function () {

                closeMeetingDetails();

                await deleteMeeting(
                    selectedMeeting.id
                );
            };

    } else {

        deleteButton.classList.add(
            "hidden"
        );
    }


    document
        .getElementById(
            "meetingDetailsModal"
        )
        .classList.remove("hidden");
}


// ===============================
// CLOSE DETAILS
// ===============================

function closeMeetingDetails() {

    document
        .getElementById(
            "meetingDetailsModal"
        )
        .classList.add("hidden");

    selectedMeeting =
        null;
}


// ===============================
// COPY CURRENT CODE
// ===============================

function copyCurrentMeetingCode() {

    if (!selectedMeeting) {
        return;
    }

    copyMeetingCode(
        selectedMeeting.meetingCode
    );
}

// ===============================
// OPEN MEETING
// ===============================

function openMeeting(meeting) {

    if (!meeting) {
        return;
    }

    if (
        meeting.status === "COMPLETED"
    ) {

        showToast(
            "This meeting has already ended.",
            "error"
        );

        return;
    }

    if (!meeting.meetingCode) {

        showToast(
            "Meeting code is not available.",
            "error"
        );

        return;
    }

    const room =
        window.open(
            "meeting-room.html?code=" +
            encodeURIComponent(
                meeting.meetingCode
            ),
            "_blank"
        );

    if (!room) {

        showToast(
            "Please allow popups for ChatApp.",
            "error"
        );
    }
}

// ===============================
// DELETE MEETING
// ===============================

async function deleteMeeting(meetingId) {

    if (!meetingId) {
        return;
    }

    const confirmed =
        confirm(
            "Are you sure you want to delete this meeting?"
        );

    if (!confirmed) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/meetings/" +
                meetingId +
                "?requesterId=" +
                currentUser.id,
                {
                    method: "DELETE"
                }
            );

        const text =
            await response.text();

        let data = {};

        try {

            data =
                JSON.parse(text);

        } catch {
        }

        if (!response.ok) {

            showToast(
                data.message ||
                text ||
                "Unable to delete meeting.",
                "error"
            );

            return;
        }

        showToast(
            "Meeting deleted successfully.",
            "success"
        );

        await loadMeetings();

    } catch (error) {

        console.error(
            "Delete meeting error:",
            error
        );

        showToast(
            "Unable to delete meeting.",
            "error"
        );
    }
}


// ===============================
// JOIN MEETING BY CODE
// ===============================

async function joinMeetingByCode() {

    const code =
        prompt(
            "Enter meeting code:"
        );

    if (!code || !code.trim()) {
        return;
    }

    try {

        const response =
            await fetch(
                API +
                "/meetings/code/" +
                encodeURIComponent(
                    code.trim()
                )
            );

        if (!response.ok) {

            const text =
                await response.text();

            throw new Error(
                text ||
                "Meeting not found."
            );
        }

        const meeting =
            await response.json();

        if (
            meeting.status ===
            "COMPLETED"
        ) {

            showToast(
                "This meeting has already ended.",
                "error"
            );

            return;
        }

        openMeeting(
            meeting
        );

    } catch (error) {

        console.error(
            "Join meeting error:",
            error
        );

        showToast(
            error.message ||
            "Meeting not found.",
            "error"
        );
    }
}


// ===============================
// COPY MEETING CODE
// ===============================

async function copyMeetingCode(code) {

    if (!code) {
        return;
    }

    try {

        await navigator.clipboard.writeText(
            code
        );

        showToast(
            "Meeting code copied.",
            "success"
        );

    } catch (error) {

        console.error(
            error
        );

        prompt(
            "Copy meeting code:",
            code
        );
    }
}


async function copyMeetingLink(code) {

    if (!code) {
        return;
    }

    const link =
        window.location.origin +
        "/meeting-room.html?code=" +
        encodeURIComponent(code);

    try {

        await navigator.clipboard.writeText(
            link
        );

        showToast(
            "Meeting link copied.",
            "success"
        );

    } catch (error) {

        console.error(
            "Copy meeting link error:",
            error
        );

        prompt(
            "Copy meeting link:",
            link
        );
    }
}

// ===============================
// MODALS
// ===============================

function closeModals() {

    document
        .getElementById("groupModal")
        .classList.add("hidden");

    document
        .getElementById("meetingModal")
        .classList.add("hidden");

    const addMemberModal =
        document.getElementById("addMemberModal");

    if (addMemberModal) {
        addMemberModal.classList.add("hidden");
    }

    const groupInfoModal =
        document.getElementById("groupInfoModal");

    if (groupInfoModal) {
        groupInfoModal.classList.add("hidden");
    }
}

// ===============================
// ONLINE STATUS / HEARTBEAT
// ===============================

async function sendHeartbeat() {

    if (!currentUser || !currentUser.id) {
        return;
    }

    try {

        const response = await fetch(
            API +
            "/auth/heartbeat/" +
            currentUser.id,
            {
                method: "POST"
            }
        );

        if (!response.ok) {
            return;
        }

        currentUser.status = "ONLINE";

        localStorage.setItem(
            "chatUser",
            JSON.stringify(currentUser)
        );

    } catch (error) {

        console.error(
            "Heartbeat failed:",
            error
        );
    }
}


function startHeartbeat() {

    if (heartbeatTimer) {
        clearInterval(heartbeatTimer);
    }

    // Immediately mark online
    sendHeartbeat();

    // Every 10 seconds
    heartbeatTimer =
        setInterval(() => {

            sendHeartbeat();

        }, 10000);
}


function stopHeartbeat() {

    if (heartbeatTimer) {

        clearInterval(
            heartbeatTimer
        );

        heartbeatTimer = null;
    }
}

// ===============================
// LOGOUT
// ===============================

async function logout() {

    if (!currentUser) {
        return;
    }

    try {

        await fetch(
            API +
            "/auth/logout/" +
            currentUser.id,
            {
                method: "POST"
            }
        );

    } catch (error) {

        console.log(error);
    }

    if (messageRefreshTimer) {
        clearInterval(messageRefreshTimer);
    }

    stopHeartbeat();

    if (statusRefreshTimer) {
        clearInterval(statusRefreshTimer);
    }

    localStorage.removeItem(
        "chatUser"
    );

    location.reload();
}


// ===============================
// NOTIFICATION DOT
// ===============================

function updateNotificationDot(count) {

    const dot =
        document.getElementById(
            "notificationDot"
        );

    if (!dot) {
        return;
    }

    if (count > 0) {

        dot.classList.remove("hidden");

        dot.textContent =
            count > 9
                ? "9+"
                : count;

    } else {

        dot.classList.add("hidden");

        dot.textContent = "";
    }
}

// ===============================
// TOAST
// ===============================

function showToast(message, type = "info") {

    let toast =
        document.getElementById("toast");

    if (!toast) {

        toast =
            document.createElement("div");

        toast.id = "toast";

        document.body.appendChild(toast);
    }

    toast.textContent = message;

    toast.className =
        "toast " + type;

    clearTimeout(
        toast.hideTimer
    );

    toast.hideTimer =
        setTimeout(() => {

            toast.className =
                "toast hidden";

        }, 3000);
}



function closeGroupInfo() {

    document
        .getElementById("groupInfoModal")
        .classList.add("hidden");
}


// ===============================
// HELPERS
// ===============================

function formatTime(value) {

    if (!value) {
        return "";
    }

    try {

        return new Date(value)
            .toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit"
            });

    } catch {

        return "";
    }
}


function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value ?? "";

    return div.innerHTML;
}


// ===============================
// START
// ===============================

window.onload = function () {

    const saved =
        localStorage.getItem(
            "chatUser"
        );

    if (saved) {

        try {

            currentUser =
                JSON.parse(saved);

            openChatPage();

        } catch {

            localStorage.removeItem(
                "chatUser"
            );
        }

    } else {

        showLogin();
    }
};