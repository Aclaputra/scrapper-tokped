import pandas as pd
import time
from selenium import webdriver
from bs4 import BeautifulSoup
import pandas as pd
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
from selenium.common.exceptions import NoSuchElementException 

df = pd.read_csv('categories_v2.csv', usecols = ['id', 'name', 'level', 'parent', 'url'])

data = []
driver = webdriver.Chrome()

for index, row in df.iterrows():
    idx, name = index,row['name']*1
    
    if not pd.isna(row['url']):
        # url by category
        driver.get(row['url'])
        print("Category Id of",row['id'], "name", row['name'])
        print("=============================================")
        
        WebDriverWait(driver, 5).until(EC.presence_of_element_located((By.CSS_SELECTOR, "#zeus-root")))
        time.sleep(2)

        for j in range(17):
            driver.execute_script("window.scrollBy(0, 250)")
            time.sleep(1)
            
        soup = BeautifulSoup(driver.page_source, "html.parser")
        
        def IsTokopedia(string):
            if string.startswith("https://www.tokopedia.com"):
                return True
            return False
        
        for item in soup.find_all('div', class_='css-bk6tzz e1nlzfl2'):
            anchor = item.find('a', class_='css-54k5sq')
            url = anchor.get("href")
            
            if not IsTokopedia(url):
                continue
            
            # url product detail to scrape
            driver.get(url)
            y = 1000
            driver.execute_script("window.scrollTo(0, "+str(y)+")")
            time.sleep(0.5)
            driver.implicitly_wait(20)
            y += 1000
            driver.execute_script("window.scrollTo(0, "+str(y)+")")
            time.sleep(0.5)
            
            title = ''
            description = ''
            price = ''
            
            time.sleep(2)
            try:
                mainImageTag = driver.find_element(By.CSS_SELECTOR, '[data-testid="PDPMainImage"]')
                mainImageUrl = mainImageTag.get_attribute("src")
                
                title = driver.find_element(By.CSS_SELECTOR, '[data-testid="lblPDPDetailProductName"]')
                description = driver.find_element(By.CSS_SELECTOR, '[data-testid="lblPDPDescriptionProduk"]')
                price = driver.find_element(By.CSS_SELECTOR, '[data-testid="pdpProductPrice"]')
                image_urls = driver.find_elements(By.CSS_SELECTOR, '[data-testid="PDPImageThumbnail"]')
                
                try:
                    print("variant found")
                    variant_title = driver.find_element(By.CSS_SELECTOR, '[data-testid="pdpVariantTitle#0"]')
                    variants = driver.find_elements(By.CSS_SELECTOR, '[data-testid="pdpVariantContainer"]')
                    print(variant_title, "title")
                    
                    for variant in variants:
                        variantDiv = variant.find_element(By.CLASS_NAME, 'css-1b2d3hk')
                        variantDiv2 = variantDiv.find_element(By.TAG_NAME, 'css-1b2d3hk')
                        variantDiv3 = variantDiv2.find_element(By.CLASS_NAME, 'css-hayuji')
                        variantDiv4 = variantDiv3.find_elements(By.CLASS_NAME, 'css-1y1bj62')
                        
                        for value in variantDiv4:
                            variantButton = value.find_element(By.CLASS_NAME, 'css-uv3tea-unf-chip')
                            print(variantButton.text)
                            
                except NoSuchElementException:
                    print("variant not found")
                    
                # if not driver.find_element(By.CSS_SELECTOR, '[data-testid="pdpVariantTitle#0"]').isEmpty():
            except:
                print("== an error occured ==")
            
            try:
                title = title.text
                description = description.text
                price = ((price.text).replace('Rp', '')).replace('.',',')
            except:
                print("== an error occured ==")
            
            print("title: ", title)
            print("description: ", description)
            print("price: ", price)
            print("list images:")
            # append to 
            imgIdx = 1
            for image in image_urls:
                imageDiv = image.find_element(By.TAG_NAME, 'div')
                imageDiv2 = imageDiv.find_element(By.CLASS_NAME, 'css-bqlp8e')
                actualImage = imageDiv2.find_element(By.CLASS_NAME, 'css-1c345mg')
                theUrl = actualImage.get_attribute("src")
                print(imgIdx,"|",theUrl)
                imgIdx += 1
                
            # if variantFound:
            #     print(variantFound, "FOUND", variant_title)
            # print("VARIANT",variants)
            # print("title", variant_title)
           
            
            
            time.sleep(2)
            
df = pd.DataFrame(data, columns=['id', 'name', 'level', 'parent', 'url'])
print(df)

df.to_csv('test2.csv', encoding='utf-8')
            
driver.close()