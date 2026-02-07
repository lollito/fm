from playwright.sync_api import sync_playwright
import json

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    # 1. Inject Authentication
    page.add_init_script("""
        localStorage.setItem('user', JSON.stringify({
            username: 'admin',
            roles: ['ROLE_ADMIN'],
            accessToken: 'fake-token'
        }));
    """)

    # 2. Mock API Responses
    # Mock Dashboard Stats
    page.route("**/api/admin/users/dashboard", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body=json.dumps({
            "totalUsers": 150,
            "activeUsers": 120,
            "verifiedUsers": 100,
            "bannedUsers": 5,
            "newUsersLast24Hours": 10,
            "activeSessionsCount": 25
        })
    ))

    # Mock Users List
    page.route("**/api/admin/users?*", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body=json.dumps({
            "content": [
                {
                    "id": 1,
                    "username": "user1",
                    "email": "user1@example.com",
                    "firstName": "John",
                    "lastName": "Doe",
                    "isActive": True,
                    "isVerified": True,
                    "isBanned": False,
                    "roles": [{"name": "ROLE_USER"}],
                    "createdDate": "2023-01-01T10:00:00",
                    "lastLoginDate": "2023-02-01T12:00:00"
                },
                {
                    "id": 2,
                    "username": "banned_user",
                    "email": "banned@example.com",
                    "firstName": "Jane",
                    "lastName": "Smith",
                    "isActive": True,
                    "isVerified": True,
                    "isBanned": True,
                    "roles": [{"name": "ROLE_USER"}],
                    "createdDate": "2023-01-05T10:00:00",
                    "lastLoginDate": "2023-01-20T12:00:00"
                }
            ],
            "totalElements": 2,
            "totalPages": 1,
            "size": 20,
            "number": 0
        })
    ))

    # 3. Navigate
    print("Navigating to /users...")
    page.goto("http://localhost:3000/users")

    # 4. Wait for content
    print("Waiting for content...")
    try:
        page.wait_for_selector("h1:has-text('User Management')", timeout=10000)
        page.wait_for_selector(".stat-card", timeout=10000) # Wait for stats
        page.wait_for_selector("table", timeout=10000) # Wait for table
    except Exception as e:
        print(f"Error waiting for selectors: {e}")
        page.screenshot(path="/app/error_screenshot.png")
        raise e

    # 5. Screenshot
    print("Taking screenshot...")
    page.screenshot(path="/app/verification_user_management.png", full_page=True)

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
