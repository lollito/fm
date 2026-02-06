import os
from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    context = browser.new_context()

    # Mock API responses
    def handle_player_request(route):
        route.fulfill(
            status=200,
            content_type="application/json",
            body='[{"id": 1, "name": "Alino", "surname": "Bartolini", "role": "GOALKEEPER", "age": 28, "stamina": 84, "playmaking": 30, "scoring": 7, "winger": 6, "passing": 20, "defending": 72, "condition": 100}]'
        )

    def handle_club_request(route):
        route.fulfill(
            status=200,
            content_type="application/json",
            body='{"id": 1, "name": "My Club", "team": {"id": 1}}'
        )

    def handle_injuries_request(route):
         route.fulfill(
            status=200,
            content_type="application/json",
            body='[{"id": 1, "playerName": "Mario", "playerSurname": "Rossi", "type": "MUSCLE_STRAIN", "severity": "MINOR", "expectedRecoveryDate": "2026-02-10", "status": "ACTIVE", "description": "Minor muscle strain"}]'
        )

    # Set user in localStorage
    page = context.new_page()
    page.add_init_script("""
        localStorage.setItem('user', JSON.stringify({
            username: 'testuser',
            id: 1,
            roles: ['ROLE_USER'],
            clubId: 1
        }));
    """)

    # Remove webpack overlay
    page.add_init_script("""
        window.addEventListener('DOMContentLoaded', () => {
            const style = document.createElement('style');
            style.innerHTML = `
                iframe[style*="z-index: 2147483647"] { display: none !important; }
            `;
            document.head.appendChild(style);
        });
    """)

    page.route("**/api/player/", handle_player_request)
    page.route("**/api/club/1", handle_club_request)
    # Handle both team and club injury endpoints just in case
    page.route("**/api/injuries/team/1", handle_injuries_request)
    page.route("**/api/injuries/club/1", handle_injuries_request)

    try:
        page.goto("http://localhost:3000/team")
        page.wait_for_load_state("networkidle")

        # Take screenshot of Team page
        page.screenshot(path="/home/jules/verification/team_page_v2.png")
        print("Team page screenshot taken")

        # Click Action button (the new .btn-icon)
        page.click("tr:first-child button.btn-icon")

        # Wait for modal
        page.wait_for_selector(".modal-content")

        # Take screenshot of Modal
        page.screenshot(path="/home/jules/verification/modal_open_v2.png")
        print("Modal screenshot taken")

    except Exception as e:
        print(f"Error: {e}")
        import traceback
        traceback.print_exc()
        page.screenshot(path="/home/jules/verification/error.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
