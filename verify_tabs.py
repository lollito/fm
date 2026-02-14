from playwright.sync_api import sync_playwright

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()

    # Mock user in localStorage
    page.add_init_script("""
        localStorage.setItem('user', JSON.stringify({
            username: 'test_manager',
            token: 'dummy_token',
            roles: ['ROLE_USER'],
            club: { id: 1, name: 'Test Club' }
        }));
    """)

    # Mock Player History API
    page.route("**/api/player-history/player/1", lambda route: route.fulfill(
        status=200,
        content_type="application/json",
        body="""{
            "player": {
                "id": 1,
                "name": "Test",
                "surname": "Player",
                "age": 20,
                "role": "STRIKER",
                "condition": 100,
                "moral": 100
            },
            "careerStats": {
                "totalMatchesPlayed": 10,
                "totalGoals": 5,
                "totalAssists": 2,
                "clubsPlayed": 1,
                "leagueTitles": 0,
                "highestTransferValue": 1000000
            },
            "seasonStats": [],
            "achievements": [],
            "transferHistory": []
        }"""
    ))

    # Mock other APIs to prevent errors
    page.route("**/api/notifications/unread", lambda route: route.fulfill(json=[]))
    page.route("**/api/manager/profile", lambda route: route.fulfill(json={}))
    page.route("**/ws/**", lambda route: route.abort()) # WebSocket

    try:
        page.goto("http://localhost:3000/player/1")
        page.wait_for_selector(".player-history", timeout=30000)

        # Check accessibility attributes
        tabs = page.locator("[role='tab']")
        count = tabs.count()
        print(f"Found {count} tabs")

        for i in range(count):
            tab = tabs.nth(i)
            role = tab.get_attribute("role")
            aria_selected = tab.get_attribute("aria-selected")
            aria_controls = tab.get_attribute("aria-controls")
            tab_id = tab.get_attribute("id")
            name = tab.inner_text()
            print(f"Tab: {name}, role={role}, aria-selected={aria_selected}, aria-controls={aria_controls}, id={tab_id}")

        # Screenshot
        page.screenshot(path="verification.png")
        print("Screenshot saved to verification.png")

    except Exception as e:
        print(f"Error: {e}")
        page.screenshot(path="error.png")

    browser.close()

with sync_playwright() as playwright:
    run(playwright)
