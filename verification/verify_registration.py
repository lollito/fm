from playwright.sync_api import sync_playwright, expect

def run(playwright):
    browser = playwright.chromium.launch(headless=True)
    page = browser.new_page()
    try:
        page.goto("http://localhost:3000/register")

        # Check for Server select
        # Assuming <select name="serverId">
        server_select = page.locator('select[name="serverId"]')
        expect(server_select).to_be_visible()

        # Wait for options to load (might take a sec for fetch)
        page.wait_for_timeout(2000)

        # Check if options exist
        options = server_select.locator('option')
        count = options.count()
        print(f"Found {count} options")

        # Expect at least "Select Server" and "Alpha"
        if count < 2:
            print("Error: No server options found")
        else:
            print("Success: Server options found")
            # Check text of options
            texts = options.all_text_contents()
            print(f"Options: {texts}")

        page.screenshot(path="verification_server_select.png")
    except Exception as e:
        print(f"Error: {e}")
    finally:
        browser.close()

with sync_playwright() as playwright:
    run(playwright)
