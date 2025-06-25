// 탭 전환
function showTab(tabId, btn) {
    document.querySelectorAll('.tab-pane').forEach(el => el.style.display = 'none');
    document.getElementById(tabId).style.display = '';
    document.querySelectorAll('.tab-menu button').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
}

// 모달 ESC 닫기
window.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') document.getElementById('editModal').style.display = 'none';
});

// 이미지 미리보기
function previewCover(e) {
    const file = e.target.files[0];
    if (file) {
        document.getElementById('coverPreview').src = URL.createObjectURL(file);
    }
}
function previewProfile(e) {
    const file = e.target.files[0];
    if (file) {
        document.getElementById('profilePreview').src = URL.createObjectURL(file);
    }
}

// 공개범위 커스텀 드롭다운
function toggleDropdown() {
    const dropdown = document.querySelector('.custom-dropdown');
    dropdown.classList.toggle('open');
}
function selectPrivacy(value, label) {
    document.getElementById('privacyInput').value = value;
    document.getElementById('dropdownLabel').innerText = label;
    document.querySelector('.custom-dropdown').classList.remove('open');
}
document.addEventListener('click', function(e) {
    const dropdown = document.querySelector('.custom-dropdown');
    if (dropdown && !dropdown.contains(e.target)) {
        dropdown.classList.remove('open');
    }
});
function openModal() {
    document.getElementById('editModal').style.display = 'block';
    document.body.classList.add('modal-open');
}
function closeModal() {
    document.getElementById('editModal').style.display = 'none';
    document.body.classList.remove('modal-open');
}
window.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') closeModal();
});

